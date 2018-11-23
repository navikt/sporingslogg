package no.nav.sporingslogg.web.fitnesse;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;

public class LocalKafkaProducerFixture {
	
	private static final KafkaProperties EMBEDDED_PROPERTIES = StandaloneTestJettyMain.getKafkaEmbeddedProperties();

	public boolean produce(String json) {
		System.out.println("Fikk json: " + json);
		Map<String, Object> senderPropsForEmbeddedKafka = getSenderPropsForEmbeddedKafka();
		sendMessages(senderPropsForEmbeddedKafka, EMBEDDED_PROPERTIES.getBootstrapServers(), EMBEDDED_PROPERTIES.getTopic(), json);
		return true;
	}

	public static Map<String, Object> getSenderPropsForEmbeddedKafka() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, EMBEDDED_PROPERTIES.getBootstrapServers());		
		return senderProps;
	}
	
	public static Map<String, Object> getGeneralSenderProps() {
		Map<String, Object> senderProps = new HashMap<>();
		senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return senderProps;
	}
	public void sendMessages(Map<String, Object> senderProps, String server, String topic, String json) {		
		System.out.println("################################### Oppretter producer mot server: " + server);		
		KafkaProducer<Integer, String> producer = new KafkaProducer<>(senderProps);		
		
		System.out.println("################################### sender meldinger ");		
		try {
			producer.send(lagTestKafkaRecord(json, topic)).get();
		} catch (Exception e) {
			throw new RuntimeException("Kunne ikke sende melding", e);
		} finally {
			producer.close();
		}
		System.out.println("################################### melding sendt, avslutter");	

	}
	private ProducerRecord<Integer, String> lagTestKafkaRecord(String json, String topic) {
		return new ProducerRecord<Integer, String>(topic, json);
	}
}
