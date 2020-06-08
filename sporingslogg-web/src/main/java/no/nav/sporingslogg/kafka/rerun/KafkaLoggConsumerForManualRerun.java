package no.nav.sporingslogg.kafka.rerun;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
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

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.restapi.LoggConverter;
import no.nav.sporingslogg.restapi.LoggMelding;
import no.nav.sporingslogg.tjeneste.LoggTjeneste;

abstract class KafkaLoggConsumerForManualRerun {
	
	/*
	 * Samme funksjonalitet som vanlig consumer (mye duplisert kode),
	 * men leser alle records den finner fra begynnelsen, med egen groupid, og stopper deretter (når ingen nye leses i poll).
	 * Meldinger som får true fra skalLagres() vil bli lagret etter at de er sendt gjennom fix().
	 * Logger keys for de som lagres, samt totalt antall lagret/feilet/ignorert.
	 * 
	 * Eksekveres ved å kalle performPollAndProcessing() med dryRun-parameter.
	 */

    private static final String SPORINGSLOGG_MANUAL_RERUN_GROUPID = "SPORINGSLOGG_MANUAL_RERUN_GROUPID";

	private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
	private LoggTjeneste loggTjeneste;
		
    private final KafkaProperties kafkaProperties;
    private KafkaConsumer<Integer, String> kafkaConsumer;    
    private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {		
	    @Override
		public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	        return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_DATE_TIME);
	    }
	}).create();
    
    KafkaLoggConsumerForManualRerun(KafkaProperties kafkaProperties) {
    	this.kafkaProperties = kafkaProperties;
    }
    
    abstract boolean skalLagres(LoggMelding loggMelding);
	abstract LoggMelding fix(LoggMelding loggMelding);
	
    public String performPollAndProcessing(boolean dryRun) {
	    Properties props = setProperties(kafkaProperties);
	    // Fjern passord fra props før display, bruk samme kode som i setProperties
	    Properties propsToDisplay = new Properties(props);
	    propsToDisplay.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+kafkaProperties.getUsername()+"' password='.......';");
	    log.info("Kafka RERUN consumer bruker props: " + propsToDisplay);
	    kafkaConsumer = new KafkaConsumer<>(props);
		return pollAndProcess(dryRun, kafkaProperties.getTopic());
    }

	private String pollAndProcess(boolean dryRun, String topic) {
		String label = "DRYRUN";
		if (!dryRun) {
			label = "REAL-UPDATE";
		}
		log.info("Starter "+label+" RERUN mot topic " + topic);
		
		int lagret = 0;
		int feilet = 0;
		int ignorert = 0;
//		kafkaConsumer.subscribe(Collections.singletonList(topic)); 
		TopicPartition topicPartition = new TopicPartition(topic, 0);
		List<TopicPartition> partitions = Arrays.asList(topicPartition); 
		kafkaConsumer.assign(partitions); 
		kafkaConsumer.seekToBeginning(new ArrayList<TopicPartition>());
		ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofSeconds(30));   
		while (records != null && records.count() > 0) { 						
			if (records.count() > 1) {
				// Skal ikke kunne skje, men i rottetilfelle så kjører vi rollback og håper det retter seg ved neste poll
				log.error("RERUN Lest mer enn 1 melding i batch fra Kafka, legger dem tilbake på topic");
				rollbackToRecord(topic, records.iterator().next());
			} else if (records.count() == 1) {
				LagretStatus s = prosesserMelding(dryRun, topic, records.iterator().next());
				if (s == LagretStatus.LAGRET_OK) lagret++;
				if (s == LagretStatus.FEILET) feilet++;
				if (s == LagretStatus.IGNORERT) ignorert++;
			}
			records = kafkaConsumer.poll(Duration.ofSeconds(10));
		} 
		String report = "RERUN " +label+": Antall lagret, feilet, ignorert: " + lagret + ", " + feilet + ", " + ignorert;
		log.info(report);
		
		log.info("Stopper RERUN Kafka consumer mot topic " + topic);
		kafkaConsumer.close();
		log.info("RERUN " +label+" Prosessering ferdig mot topic " + topic);
		return report;
	}

	private LagretStatus prosesserMelding(boolean dryRun, String topic, ConsumerRecord<Integer, String> record) {		
		String json = record.value();		
		LoggMelding loggMelding = null;			
		try {
			loggMelding = gson.fromJson(json, LoggMelding.class);
		} catch (Exception e) {
			String jsonTilLogging = json;
			if (jsonTilLogging != null && jsonTilLogging.length() > 100) {
				jsonTilLogging = jsonTilLogging.substring(0,100) + ".....";
			}
			log.error("RERUN Mottatt sporingsmelding kan ikke deserialiseres, må evt rettes og sendes inn på nytt: "+jsonTilLogging, e);
			// Commit så ikke userialiserbar melding leses på nytt
			kafkaConsumer.commitSync();
			return LagretStatus.FEILET;
		}
		
		return lagreMelding(dryRun, topic, record, loggMelding);
	}

	private enum LagretStatus {
		LAGRET_OK,
		FEILET,
		IGNORERT;
	}
	
	private LagretStatus lagreMelding(boolean dryRun, String topic, ConsumerRecord<Integer, String> record, LoggMelding loggMelding) {		
		if (loggMelding != null) {
			if (skalLagres(loggMelding)) {
				try {
					if (dryRun) {
						log.info("RERUN DRYRUN, Ville lagret melding med KEY: " + record.key());
					} else {
						loggMelding = fix(loggMelding);
						loggTjeneste.lagreLoggInnslag(LoggConverter.fromJsonObject(loggMelding));
						log.info("RERUN Prosessert melding med KEY: " + record.key());
					}
					kafkaConsumer.commitSync();
					return LagretStatus.LAGRET_OK;
					
				} catch (IllegalArgumentException e) {
					String melding = "ID: " + loggMelding.getId() + ", person: " + LoggTjeneste.scrambleFnr(loggMelding.getPerson()) + ", tema: " + loggMelding.getTema() + ", mottaker: " + loggMelding.getMottaker();
					log.error("RERUN Mottatt sporingsmelding med KEY "+record.key()+" har ugyldig innhold, må evt prosesseres på nytt: " + melding, e);
					// Skal commite her, så ikke ugyldig melding leses på nytt
					kafkaConsumer.commitSync();
					return LagretStatus.FEILET;
					
				} catch (Exception e) {
					// Her må det være (teknisk) feil med å lagre i DB, rollback og prøv igjen til OK eller app'en restartes
					String melding = "ID: " + loggMelding.getId() + ", person: " + LoggTjeneste.scrambleFnr(loggMelding.getPerson()) + ", tema: " + loggMelding.getTema() + ", mottaker: " + loggMelding.getMottaker();
					log.warn("RERUN Lagring av sporingsmelding med KEY "+record.key()+" feiler, må evt prosesseres på nytt: " + melding, e);
					kafkaConsumer.commitSync();
					return LagretStatus.FEILET;
				}
			} else {
//				log.info("Ignorert melding med KEY: " + record.key());
				return LagretStatus.IGNORERT;
			}
		} else {
			log.info("RERUN NULL melding kan ikke prosesseres");
			return LagretStatus.FEILET;
		}
	}

	private void rollbackToRecord(String topic, ConsumerRecord<Integer, String> record) {
		// commit gir beskjed til server om at meldingen er prosessert for denne consgruppa,
		// men selv uten commit vil consumer/klient peke forbi offset'en inntil server rebalanserer e.l
		// https://stackoverflow.com/questions/34901781/kafka-0-9-how-to-re-consume-message-when-manually-committing-offset-with-a-kafka
		// Må dermed sette offset tilbake for å utføre "rollback" på klienten
		kafkaConsumer.seek(new TopicPartition(topic, record.partition()), record.offset());
	}

	private Properties setProperties(KafkaProperties kafkaProperties) {
		final Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, SPORINGSLOGG_MANUAL_RERUN_GROUPID);
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
}
