package no.nav.sporingslogg.kafka.rerun;

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.restapi.LoggMelding;

public class RerunUFOwithOrgnrPrefix extends KafkaLoggConsumerForManualRerun {
	
    RerunUFOwithOrgnrPrefix(KafkaProperties kafkaProperties) {
		super(kafkaProperties);
	}
    
    // Rekjører/henter fra topic meldinger med tema "UFO" og 14-tegns orgnr som starter med "0192:",
    // fjerner iorgnr-prefiks før lagring.
	boolean skalLagres(LoggMelding loggMelding) {
		return "UFO".equals(loggMelding.getTema()) 
				&& loggMelding.getMottaker().length() == 14 
				&& loggMelding.getMottaker().startsWith("0192:");
	}
	
	LoggMelding fix(LoggMelding loggMelding) {
		loggMelding.setMottaker(loggMelding.getMottaker().substring(5));
		return loggMelding;
	}
	
}
