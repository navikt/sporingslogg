package no.nav.sporingslogg.kafka;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.sporingslogg.restapi.CustomJsonObjectMapper;
import no.nav.sporingslogg.restapi.LoggMelding;

public class LoggmeldingJsonMapper implements Serializer<LoggMelding>, Deserializer<LoggMelding> {

    private final Logger log = LoggerFactory.getLogger(getClass());
	private final ObjectMapper objectMapper;
		
	public LoggmeldingJsonMapper() {
		objectMapper = new CustomJsonObjectMapper();
	}

	@Override
	public void close() {
	}

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {
	}

	@Override
	public LoggMelding deserialize(String arg0, byte[] json) {
		try {
			return objectMapper.readValue(json, LoggMelding.class);
		} catch (Exception e) {
			// Får ikke til å håndtere exceptions i poll-kallet, håndter NULL istedenfor
			//throw new RuntimeException("Kan ikke parse json til LoggMelding", e);
			log.error("Exception ved prosessering av loggmelding, må evt rettes og sendes inn på nytt", e);
			return null;
		}
	}

	@Override
	public byte[] serialize(String arg0, LoggMelding loggMelding) {
		try {
			return objectMapper.writeValueAsBytes(loggMelding);
		} catch (Exception e) {
			throw new RuntimeException("Kan ikke lage json av LoggMelding", e);
		}
	}
}