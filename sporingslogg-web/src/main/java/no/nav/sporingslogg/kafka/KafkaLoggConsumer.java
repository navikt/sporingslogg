package no.nav.sporingslogg.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import no.nav.sporingslogg.restapi.LoggConverter;
import no.nav.sporingslogg.restapi.LoggMelding;
import no.nav.sporingslogg.tjeneste.LoggTjeneste;

public class KafkaLoggConsumer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
	LoggTjeneste loggTjeneste;
		
    private final KafkaConsumer<Integer, LoggMelding> kafkaConsumer;
    
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

	public Properties setProperties(KafkaProperties kafkaProperties) {
		final Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LoggmeldingJsonMapper.class);
	    
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
						ConsumerRecords<Integer, LoggMelding> records = null;
//						try {  // Denne try-catchen får polling til å gå i loop..... har byttet ut med null-return fra Json-deserialisering istedenfor
							records = kafkaConsumer.poll(Duration.ofSeconds(10));   
//						} catch (Exception e) {
//							kafkaConsumer.commitSync(); // hjelper ikke, går i loop likevel...
//							log.error("Exception ved prosessering av loggmelding, må evt rettes og sendes inn på nytt", e);
//						}
						lagreMeldinger(records);
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
	
	private void lagreMeldinger(ConsumerRecords<Integer, LoggMelding> records) {
					
		if (records != null) {
//			log.debug("Polling ferdig, ga "+records.count() + " records");
			try {
				for (ConsumerRecord<Integer, LoggMelding> record : records) {                
					LoggMelding loggMelding = record.value();
					if (loggMelding != null) {
						store(loggMelding);
					} else {
	//					log.debug("Kunne ikke lese/parse loggmelding");
					}
				}
				// TODO dette skal forbedres   Siden man må lese ukjent antall records, kan man ikke commite en og en fra kafka.
				// Dermed må man uansett late som alle ble lest OK og logge de som feilet for å få sendt dem på nytt.
				kafkaConsumer.commitSync();
			} catch (Exception e) {
				log.error("Feilet ved forsøk på å lagre melding fra topic til DB, meldingen vil fortsatt ligge på topic og vil forsøkes igjen", e);
			}
		} else {
//			log.debug("Polling ferdig, kunne ikke lese melding(er)");
		}
	}

	public void store(LoggMelding loggMelding) {
//		log.debug("Lagrer sporingsmelding mottatt via Kafka.");
		try {
			loggTjeneste.lagreLoggInnslag(LoggConverter.fromJsonObject(loggMelding));
		} catch (Exception e) {
			String melding = "ID: " + loggMelding.getId() + ", person: " + LoggTjeneste.scrambleFnr(loggMelding.getPerson()) + ", tema: " + loggMelding.getTema() + ", mottaker: " + loggMelding.getMottaker();
			log.error("Lagring av sporingsmelding feiler, må evt sendes inn på nytt: " + melding, e);
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
