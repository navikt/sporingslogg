package no.nav.sporingslogg.kafkatestclient;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;

public class ManualKafkaReceiver { // Motta melding(er) som sendt fra ManualKafkaSender, fra kafka startet via EmbeddedKafkaMain
	
	private static final KafkaProperties EMBEDDED_PROPERTIES = StandaloneTestJettyMain.getKafkaEmbeddedProperties();
	private static final KafkaProperties TEST_PROPERTIES = StandaloneTestJettyMain.getKafkaTestProperties();
	
	public static void main(String[] args) {
		Map<String, Object> props = getConsumerPropsForEmbeddedKafka();
	    new ManualKafkaReceiver().startPollingForever(props, EMBEDDED_PROPERTIES.getBootstrapServers(), EMBEDDED_PROPERTIES.getTopic());
	}
	
	public static Map<String, Object> getConsumerPropsForEmbeddedKafka() {
		Map<String, Object> props = getGeneralConsumerProps();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, EMBEDDED_PROPERTIES.getBootstrapServers());		
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, EMBEDDED_PROPERTIES.getGroupId());
		return props;
	}
	
	public static Map<String, Object> getConsumerPropsForTest() {
		Map<String, Object> props = getGeneralConsumerProps();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TEST_PROPERTIES.getBootstrapServers());		
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, TEST_PROPERTIES.getGroupId());
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+TEST_PROPERTIES.getUsername()
		+"' password='"+TEST_PROPERTIES.getPassword()+"';");
		props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, TEST_PROPERTIES.getTruststoreFile());
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  TEST_PROPERTIES.getTruststorePassword());
		return props;
	}
	
	public static Map<String, Object> getGeneralConsumerProps() {
		Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);		
	    return props;
	}
	
	public void startPollingForever(Map<String, Object> props, String server, String topic) {
		System.out.println("################################### Oppretter consumer mot server: " + server);		
		@SuppressWarnings("resource")
		KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(props);
		Map<String, List<PartitionInfo>> listTopics = kafkaConsumer.listTopics(Duration.ofSeconds(3));
		System.out.println("Found topics: " + listTopics.size());
		kafkaConsumer.subscribe(Collections.singletonList(topic));  				

		System.out.println("################################### Starter polle-løkke");		
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {	
			
			@Override
			public void run() {
				
				while (true) { 
					System.out.println("-- Starter polling med timeout 10 sek");
					ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofSeconds(10));   
					loggReceivedRecords(records);        
				} 
			}

		});
	}
	
	private void loggReceivedRecords(ConsumerRecords<Integer, String> records) {
		System.out.println("-- Polling ferdig, ga "+records.count() + " records");
		for (ConsumerRecord<Integer, String> record : records) {                
			System.out.println("-- Record: " + record.value());
		}
	}
}
