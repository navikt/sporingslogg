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

public class ManualKafkaReceiver { // Motta melding(er) som sendt fra ManualKafkaSender, fra kafka startet via EmbeddedKafkaMain
	
	static final String SERVER_EMBEDDED = "127.0.0.1:9092";  // SETT DENNE SOM EmbeddedKafkaMain SIER 
	
	static final String SERVER_TEST = "d26apvl00159.test.local:8443";
	static final String TOPIC_TEST = "sporingslogg";
	static final String GROUP_TEST = "KC-sporingslogg";
	
	public static void main(String[] args) {
		Map<String, Object> props = getConsumerPropsForTest();
	    new ManualKafkaReceiver().startPollingForever(props, SERVER_TEST, TOPIC_TEST);
	}
	
	public static Map<String, Object> getConsumerPropsForEmbeddedKafka() {
		Map<String, Object> props = getGeneralConsumerProps();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_EMBEDDED);		
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "someGroup");
		return props;
	}
	
	public static Map<String, Object> getConsumerPropsForTest() {
		Map<String, Object> props = getGeneralConsumerProps();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_TEST);		
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_TEST);
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='srvABACPEP' password='hUK1.30sKhqp0.(2';");
		props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "W:/workspace/sporingslogg/kafkaclient/src/test/resources/nav_truststore_nonproduction_ny2.jts");
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  "467792be15c4a8807681fd2d5c9c1748");
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
