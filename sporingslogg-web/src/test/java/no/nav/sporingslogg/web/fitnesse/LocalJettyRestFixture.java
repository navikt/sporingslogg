package no.nav.sporingslogg.web.fitnesse;

import java.util.Base64;

import smartrics.rest.fitnesse.fixture.RestFixture;
import smartrics.rest.fitnesse.fixture.support.CellWrapper;

public class LocalJettyRestFixture extends RestFixture {
	
	static int JETTY_PORT = StartFitnesseWithJettySporingsLoggWeb.JETTY_PORT;  // Må sette defaultverdi som stemmer med main()-versjonen, ellers får vi den ikke med i Fitnesse-VMen
	private static final String CONFIG_SET_PLAIN_TEXT_RESPONSE = "plainTextResponse";
		
	public LocalJettyRestFixture() {
		super("http://localhost:"+JETTY_PORT);
		addHeader("Content-Type: application/json");		
	}

	// Bruk denne constructoren hvis du trenger spesielle config-settinger (f.eks. response-json skal parses som plain text)
	public LocalJettyRestFixture(String configName) {
		super("http://localhost:"+JETTY_PORT, configName);
		addHeader("Content-Type: application/json");		
		setupFixtureConfig(configName);
	}

	public void brukernavn() {   // superklassen nekter å kalle metoder med parametre...dermed må den rare logikken i hentTabellCelleInnhold brukes (kopiert fra superklassens setBody() )
		addHeader("Authorization: "+getEncodedBasicAuth(hentTabellCelleInnhold("brukernavn"), "dummyPassord"));  // Jetty sjekker ikke passord
	}

	public void oidctoken() {
		addHeader("Authorization: Bearer "+hentTabellCelleInnhold("oidctoken")); 
	}

	public void jsonMelding() {
		setBody(hentTabellCelleInnhold("jsonMelding")); 
	}

	@SuppressWarnings("unchecked")
	private String hentTabellCelleInnhold(String label) {
		CellWrapper<?> cell = row.getCell(1);
		if (cell == null) {
			getFormatter().exception(row.getCell(0), "You must pass contents to "+label);
			return "";
		} else {
			return getFormatter().fromRaw(cell.text());
		}
	}

	private String getEncodedBasicAuth(String username, String password) {
		return "Basic " + Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes());
	}
	
	private void setupFixtureConfig(String configName) {
		if (CONFIG_SET_PLAIN_TEXT_RESPONSE.equals(configName)) {
			getConfig().add("restfixture.content.handlers.map", "application/json=TEXT");
		}
	}
}
