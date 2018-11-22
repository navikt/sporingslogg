package no.nav.sporingslogg.web.fitnesse;

import java.util.Base64;
import java.util.Map;

import smartrics.rest.fitnesse.fixture.RestFixture;
import smartrics.rest.fitnesse.fixture.support.CellWrapper;

public class ServerRestFixture extends RestFixture {
	
	public ServerRestFixture() {
		super(SetupServer.url);
		addHeader("Content-Type: application/json");		
	}

	public void jsonMelding() {
		setBody(hentTabellCelleInnhold("jsonMelding")); 
	}

	@Override
	protected void doMethod(String method, String resUrl, Map<String, String> headers, String rBody) {
		addHeader("Authorization: " + getEncodedBasicAuth(SetupServer.brukernavn(resUrl), SetupServer.passord(resUrl)));
		super.doMethod(method, resUrl, getHeaders(), rBody); // mister en substitute() i superklassen her, men vi bruker ikke global variabel-substitusjon uansett
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
}
