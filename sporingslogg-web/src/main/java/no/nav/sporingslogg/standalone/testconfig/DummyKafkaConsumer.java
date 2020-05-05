package no.nav.sporingslogg.standalone.testconfig;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.sporingslogg.kafka.KafkaLoggConsumer;
import no.nav.sporingslogg.kafka.KafkaProperties;

public class DummyKafkaConsumer extends KafkaLoggConsumer { // Dropp polling, ikke gjør noe som helst

    private final Logger log = LoggerFactory.getLogger(getClass());

    public DummyKafkaConsumer() {
		super(new KafkaProperties());
		log.info("Dummy Kafka, no polling at all");
	}

	@Override
	public Properties setProperties(KafkaProperties kafkaProperties) {
		return new Properties();
	}

	@Override	
	public void pollForever(String topic) {
		// gjesp
	}

}
