package no.nav.sporingslogg.standalone.testconfig;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.sporingslogg.kafka.KafkaLoggConsumer;
import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.kafka.LoggmeldingJsonMapper;

public class EmbeddedKafkaConsumer extends KafkaLoggConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedKafkaConsumer.class);

	public EmbeddedKafkaConsumer(KafkaProperties kafkaProperties) {
		super(kafkaProperties);
	}

	@Override
	public Properties setProperties(KafkaProperties kafkaProperties) {
		log.info("Embedded Kafka consumer");
		Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LoggmeldingJsonMapper.class);
	    return props;
	}

}
