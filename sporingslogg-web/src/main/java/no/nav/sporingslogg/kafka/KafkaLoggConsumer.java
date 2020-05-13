package no.nav.sporingslogg.kafka;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import no.nav.sporingslogg.restapi.LoggConverter;
import no.nav.sporingslogg.restapi.LoggMelding;
import no.nav.sporingslogg.tjeneste.LoggTjeneste;

public class KafkaLoggConsumer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /*
     * Feilsituasjoner og -håndtering ved polling av meldinger:
     * - kafka-melding kan ikke parses til gyldig LoggMelding: logges (ERROR) som "feil, må sendes på nytt", les/kafka committes
     * - LoggMelding er ikke OK (validering feiler): logges (ERROR) som "feil, må sendes på nytt", les/kafka committes
     * - Lagring feiler (teknisk problem): logges (WARN) som "lagring feiler, prøves på nytt", les/kafka rulles tilbake (vil bli rekjørt til den funker)
     * "Må sendes på nytt" betyr at avsender må varsles.
     * 
     * Setter opp konsument til å lese 1 og 1 melding for å ha full kontroll med commit/rollback av hver.
     */

    @Autowired
	LoggTjeneste loggTjeneste;
		
    // Leser meldinger som ren String og deserialiserer eksplisitt, for å ha full kontroll på feilhåndtering ved deserialisering.
    // Forvent ISO-format string for DateTime, ihht spec og tidligere jackson-basert logikk.
    private final KafkaConsumer<Integer, String> kafkaConsumer;    
    private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {		
	    @Override
		public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	        return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_DATE_TIME);
	    }
	}).create();
    
    public KafkaLoggConsumer(KafkaProperties kafkaProperties) {
	    Properties props = setProperties(kafkaProperties);
	    // Fjern passord fra props før display, bruk samme kode som i setProperties
	    Properties propsToDisplay = new Properties(props);
	    propsToDisplay.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+kafkaProperties.getUsername()+"' password='.......';");
	    log.info("Kafka consumer bruker props: " + propsToDisplay);
	    kafkaConsumer = new KafkaConsumer<>(props);
	    setCleanupAtShutdown();
		pollForever(kafkaProperties.getTopic());
	}

	// public: overrides i tester
	public void pollForever(String topic) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		log.info("Starter Kafka consumer mot topic " + topic);
		executorService.execute(new Runnable() {
			@Override
			public void run() {				
				try {
					kafkaConsumer.subscribe(Collections.singletonList(topic));  
					
					while (true) { 						
//						log.debug("Starter polling 10 sek");						
						ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofSeconds(10));   
						
						if (records != null) {
							if (records.count() > 1) {
								// Skal ikke kunne skje, men i rottetilfelle så kjører vi rollback og håper det retter seg ved neste poll
								log.error("Lest mer enn 1 melding i batch fra Kafka, legger dem tilbake på topic");
								rollbackToRecord(topic, records.iterator().next());
							} else if (records.count() == 1) {
								parseOgLagreMelding(topic, records.iterator().next());
							}
						}
					} 
			    } catch (WakeupException e) {
			        // Kalt fra shutdown 
			    } finally {
					log.info("Stopper Kafka consumer mot topic " + topic);
					kafkaConsumer.close();
					log.info("Kafka consumer stoppet.");
			    }
			}
		});
	}

	private void parseOgLagreMelding(String topic, ConsumerRecord<Integer, String> record) {		
//		log.debug("Polling ferdig, ga "+records.count() + " records");
		String json = record.value();		
		LoggMelding loggMelding = null;			
		try {
			loggMelding = gson.fromJson(json, LoggMelding.class);
		} catch (Exception e) {
			String jsonTilLogging = json;
			if (jsonTilLogging != null && jsonTilLogging.length() > 100) {
				jsonTilLogging = jsonTilLogging.substring(0,100) + ".....";
			}
			log.error("Mottatt sporingsmelding kan ikke deserialiseres, må evt rettes og sendes inn på nytt: "+jsonTilLogging, e);
			// Commit så ikke userialiserbar melding leses på nytt
			kafkaConsumer.commitSync();
			return;
		}
		
		lagreMelding(topic, record, loggMelding);
	}

	private void lagreMelding(String topic, ConsumerRecord<Integer, String> record, LoggMelding loggMelding) {		
		if (loggMelding != null) {
//				log.debug("Lagrer sporingsmelding mottatt via Kafka.");
			try {
				loggTjeneste.lagreLoggInnslag(LoggConverter.fromJsonObject(loggMelding));
				kafkaConsumer.commitSync();
				
			} catch (IllegalArgumentException e) {
				String melding = "ID: " + loggMelding.getId() + ", person: " + LoggTjeneste.scrambleFnr(loggMelding.getPerson()) + ", tema: " + loggMelding.getTema() + ", mottaker: " + loggMelding.getMottaker();
				log.error("Mottatt sporingsmelding har ugyldig innhold, må evt sendes inn på nytt: " + melding, e);
				// Skal commite her, så ikke ugyldig melding leses på nytt
				kafkaConsumer.commitSync();
				
			} catch (Exception e) {
				// Her må det være (teknisk) feil med å lagre i DB, rollback og prøv igjen til OK eller app'en restartes
				String melding = "ID: " + loggMelding.getId() + ", person: " + LoggTjeneste.scrambleFnr(loggMelding.getPerson()) + ", tema: " + loggMelding.getTema() + ", mottaker: " + loggMelding.getMottaker();
				log.warn("Lagring av sporingsmelding feiler, vil prøves på nytt: " + melding, e);
				rollbackToRecord(topic, record);
				// Vent litt før polling får fortsette, så feilen evt har blitt rettet
				sleepSeconds(3);
			}
		}
	}

	private void rollbackToRecord(String topic, ConsumerRecord<Integer, String> record) {
		// commit gir beskjed til server om at meldingen er prosessert for denne consgruppa,
		// men selv uten commit vil consumer/klient peke forbi offset'en inntil server rebalanserer e.l
		// https://stackoverflow.com/questions/34901781/kafka-0-9-how-to-re-consume-message-when-manually-committing-offset-with-a-kafka
		// Må dermed sette offset tilbake for å utføre "rollback" på klienten
		kafkaConsumer.seek(new TopicPartition(topic, record.partition()), record.offset());
	}

	public Properties setProperties(KafkaProperties kafkaProperties) {
		final Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    // Skal lese 1 og 1 melding, med eksplisitt commit for hver
	    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
	    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
	    
	    if (kafkaProperties.getUsername() != null) {
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
			props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+kafkaProperties.getUsername()+"' password='"+kafkaProperties.getPassword()+"';");
			props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
			props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
			
			props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getTruststoreFile());
			props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  kafkaProperties.getTruststorePassword());
	    }
		return props;
	}

	private void sleepSeconds(int seconds) {
		try {
			Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			// OK
		}
	}

	private void setCleanupAtShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    @Override
		    public void run() {
				shutdown();
		    }
		}));
	}

	// Kan kalles eksplisitt av tester
    public void shutdown() {
		kafkaConsumer.wakeup();
	}
}
