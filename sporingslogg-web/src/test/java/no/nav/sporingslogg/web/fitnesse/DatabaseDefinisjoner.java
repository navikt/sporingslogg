package no.nav.sporingslogg.web.fitnesse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseDefinisjoner {

	// Denne klassen holder rede på datatyper for db-kolonner i databasen. Se nederst for selve oppsettet
	
	public enum VerdiType {
		STRING,
		INT,
		TIMESTAMP;
	}
	
	public static List<String> kolonneNavnForTabell(String tabellNavn) {
		KolonneTyperForTabell kolonneTyperForTabell = databaseTabellDefinisjoner.get(tabellNavn.toUpperCase());
		if (kolonneTyperForTabell == null) {
			throw new RuntimeException("Det er ikke satt opp DB-typer for tabell " + tabellNavn);
		}
		return new ArrayList<String>(kolonneTyperForTabell.getKolonneNavn());
	}
	
	public static List<VerdiType> kolonneTyper(String tabellNavn, List<String> kolonneNavnListe) {
		List<VerdiType> result = new ArrayList<VerdiType>();
		for (String k : kolonneNavnListe) {
			result.add(DatabaseDefinisjoner.getKolonneType(tabellNavn, k));
		}
		return result;
	}

	private static VerdiType getKolonneType(String tabellNavn, String kolonneNavn) {
		KolonneTyperForTabell kolonneTyperForTabell = databaseTabellDefinisjoner.get(tabellNavn.toUpperCase());
		if (kolonneTyperForTabell == null) {
			throw new RuntimeException("Det er ikke satt opp DB-typer for tabell " + tabellNavn);
		}
		VerdiType type = kolonneTyperForTabell.getType(kolonneNavn);
		if (type == null) {
			throw new RuntimeException("Det er ikke satt opp DB-type for kolonne "+kolonneNavn+" i tabell " + tabellNavn);
		}
		return type;
	}
	
	private static class KolonneTyperForTabell {
		private final String tabellNavn;
		private final Map<String, VerdiType> verdiForKolonne = new HashMap<String, DatabaseDefinisjoner.VerdiType>();
		KolonneTyperForTabell(String tabellNavn) {
			this.tabellNavn = tabellNavn;
		}
		void setType(String kolonneNavn, VerdiType kolonneType) {
			verdiForKolonne.put(kolonneNavn, kolonneType);
		}
		VerdiType getType(String kolonneNavn) {
			return verdiForKolonne.get(kolonneNavn.toUpperCase());
		}
		Set<String> getKolonneNavn() {
			return verdiForKolonne.keySet();
		}
	}
	
	private static final Map<String, KolonneTyperForTabell> databaseTabellDefinisjoner = settOppDatabaseDefinisjoner();

	private static Map<String, KolonneTyperForTabell> settOppDatabaseDefinisjoner() {  // Bruk UPPER CASE overalt !
		Map<String, KolonneTyperForTabell> result = new HashMap<String, KolonneTyperForTabell>();
		KolonneTyperForTabell loggMeldinger = new KolonneTyperForTabell("SPORINGS_LOGG");
		result.put("SPORINGS_LOGG",	loggMeldinger);
		loggMeldinger.setType("ID", VerdiType.INT);
		loggMeldinger.setType("PERSON", VerdiType.STRING);
		loggMeldinger.setType("MOTTAKER", VerdiType.STRING);
		loggMeldinger.setType("TEMA", VerdiType.STRING);
		loggMeldinger.setType("HJEMMEL", VerdiType.STRING);
		loggMeldinger.setType("TIDSPUNKT", VerdiType.TIMESTAMP);
		loggMeldinger.setType("LEVERTE_DATA", VerdiType.STRING);
		loggMeldinger.setType("SAMTYKKE_TOKEN", VerdiType.STRING);
		return result;
	}
}
