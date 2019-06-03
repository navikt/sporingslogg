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
		
	public KafkaLoggConsumer(KafkaProperties kafkaProperties) {
	    Properties props = setProperties(kafkaProperties);
	    // Fjern passord fra props før display, bruk samme kode som i setProperties
	    Properties propsToDisplay = new Properties(props);
	    propsToDisplay.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+kafkaProperties.getUsername()+"' password='.......';");
	    log.info("Kafka consumer bruker props: " + propsToDisplay);
		pollForever(kafkaProperties.getTopic(), props);
	}

	public Properties setProperties(KafkaProperties kafkaProperties) {
		final Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LoggmeldingJsonMapper.class);
	    
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+kafkaProperties.getUsername()+"' password='"+kafkaProperties.getPassword()+"';");
		props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getTruststoreFile());
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  kafkaProperties.getTruststorePassword());
		return props;
	}

	public void pollForever(String topic, final Properties props) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		log.info("Starter Kafka consumer");
		executorService.execute(new Runnable() {			// TODO kan ta med en catch WakeupException og legge inn en wakeup i shutdown...
			@Override
			public void run() {
				
				@SuppressWarnings("resource")
				KafkaConsumer<Integer, LoggMelding> kafkaConsumer = new KafkaConsumer<>(props);
				kafkaConsumer.subscribe(Collections.singletonList(topic));  
				
				while (true) { 
					log.debug("Starter polling 10 sek");
//					try {
						ConsumerRecords<Integer, LoggMelding> records = kafkaConsumer.poll(Duration.ofSeconds(10));   
						log.debug("Polling ferdig, ga "+records.count() + " records");
						for (ConsumerRecord<Integer, LoggMelding> record : records) {                
							LoggMelding loggMelding = record.value();
							if (loggMelding != null) {
								store(loggMelding);
							}
						}    
//					} catch (Exception e) {
//						log.error("Exception ved prosessering av logg", e);         TODO fortsette uten å spise feilmeldingene på nytt...?
//					}
				} 
			}
		});
	}

	public void store(LoggMelding loggMelding) {
		log.debug("Lagrer sporingsmelding mottatt via Kafka: " + loggMelding);
		try {
			loggTjeneste.lagreLoggInnslag(LoggConverter.fromJsonObject(loggMelding));
		} catch (Exception e) {
			log.error("Lagring av sporingsmelding feiler, må evt sendes inn på nytt",e);
		}
	}
}
