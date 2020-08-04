package no.nav.sporingslogg.kafka.rerun;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.restapi.LoggMelding;

public class RerunPENwithMessageLongerThan100K extends KafkaLoggConsumerForManualRerun {
	
    RerunPENwithMessageLongerThan100K(KafkaProperties kafkaProperties) {
		super(kafkaProperties);
	}
    
    // Rekjører/henter fra topic meldinger med tema "PEN" og melding > 100.000 bytes, sendt inn før 07:26 3/8
	boolean skalLagres(LoggMelding loggMelding) {
		return "PEN".equals(loggMelding.getTema()) 
				&& loggMelding.getLeverteData().length() > 100000
				&& loggMelding.getUthentingsTidspunkt().isBefore(LocalDateTime.parse("2020-08-03 07:26", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
	}
	
	LoggMelding fix(LoggMelding loggMelding) {
		return loggMelding;
	}
}
