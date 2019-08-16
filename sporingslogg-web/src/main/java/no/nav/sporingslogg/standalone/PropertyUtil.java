package no.nav.sporingslogg.standalone;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(PropertyUtil.class);

    // Denne kan brukes av egen kode som eksplisitt henter properties, håndterer at Docker/Nais fucker opp propertynavnene
    public static String getProperty(String key) {
        String val = System.getProperty(key);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return val;
    }
    
    // Denne gjør at properties som injectes via Docker/Nais, også finnes med dots, men fortsatt CAPSLOCK.
    // Dvs. at kode som leter etter props med lowercase i navnet, ikke får treff.
    // Har sørget for at Spring prop-placeholders endres til CAPSLOCK, men det kan fortsatt finnes tredjeparts kode som slår opp props direkte...
    public static void fixDockerEnvProblem() {
    	Map<String, String> allEnvs = System.getenv();
    	for (String key : allEnvs.keySet()) {
			if (key.toLowerCase().contains("sporingslogg") && containsUnderscoreAndNoDots(key)) {
				String keyWithDotsInsteadOfUnderscores = key.replace('_','.');
				String value = allEnvs.get(key);
				log.info("Kopierer verdi fra env '"+key+"' til system-prop '"+keyWithDotsInsteadOfUnderscores+"'");
				System.setProperty(keyWithDotsInsteadOfUnderscores, value);
			}
		}
    }

	private static boolean containsUnderscoreAndNoDots(String s) {
		return s.indexOf('_') > -1 && s.indexOf('.') == -1;
	}
}
