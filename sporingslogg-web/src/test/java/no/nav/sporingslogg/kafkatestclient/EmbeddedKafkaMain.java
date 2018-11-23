package no.nav.sporingslogg.kafkatestclient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import kafka.server.KafkaConfig;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;

public class EmbeddedKafkaMain { 
	
	// Bruk kopiert kode fra https://www.javatips.net/api/examples-master/kafka-streams,
	// start lokal Zookeeper og kafka, og opprett topic 
	private static final String EMBEDDED_SERVER = StandaloneTestJettyMain.getKafkaEmbeddedProperties().getBootstrapServers();
	
	public static void main(String[] args) {
		
		setupAndStart(EMBEDDED_SERVER);
	}


	public static void setupAndStart(String topic) {
		System.out.println("---------------- Starter Zookeeper");
		ZooKeeperEmbedded zooKeeperEmbedded = startZooKeeper();
		
		Properties properties = new Properties();
		properties.put(KafkaConfig.ZkConnectProp(), zooKeeperEmbedded.connectString());
		// Se forklaring i bunnen av https://github.com/wurstmeister/kafka-docker/issues/218
		// Uten denne blir det masse feilmeldinger og consume funker ikke
		properties.put(KafkaConfig.OffsetsTopicReplicationFactorProp(), (short)1);
		
		System.out.println("---------------- Starter Kafka");
		KafkaEmbedded kafkaEmbedded = startKafka(properties);
		
		System.out.println("---------------- Oppretter topic "+topic);
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaEmbedded.zookeeperConnect());
	    AdminClient adminClient = AdminClient.create(props);
	    NewTopic newTopic = new NewTopic(topic, 1, (short)1);
	    adminClient.createTopics(Arrays.asList(newTopic));
		
		System.out.println("---------------- Server er klar, adresse: " + kafkaEmbedded.brokerList());
	}


	private static KafkaEmbedded startKafka(Properties properties) {
		try {
			return new KafkaEmbedded(properties);
		} catch (IOException e) {
			throw new RuntimeException("Kan ikke starte Kafka", e);
		}
	}


	private static ZooKeeperEmbedded startZooKeeper() {
		try {
			return new ZooKeeperEmbedded();
		} catch (Exception e) {
			throw new RuntimeException("Kan ikke starte Zookeeper", e);
		}
	}
}
