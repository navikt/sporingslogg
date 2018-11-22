package no.nav.sporingslogg.tjeneste;

import java.time.LocalDateTime;

public class HardCodedTimestampTjeneste extends TimestampTjeneste {

    public static String NOW = "2017-05-17T12:13:14";
    
	public LocalDateTime now() {
        return LocalDateTime.parse(NOW);
    }
}
