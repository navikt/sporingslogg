package no.nav.sporingslogg.web.fitnesse;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import smartrics.rest.fitnesse.fixture.RestFixture;
import smartrics.rest.fitnesse.fixture.support.CellWrapper;

public class ServerRestFixture extends RestFixture {
	
	boolean authSet = false;
	
	public ServerRestFixture() {
		super(SetupServer.url);
		addHeader("Content-Type: application/json");		
	}

	public void jsonMelding() {
		setBody(hentTabellCelleInnhold("jsonMelding")); 
//		setBody(longMelding()); 
	}
	
	private String longMelding() {
		return "{\"person\":\"12345678901\", \"mottaker\":\"123456789\", \"tema\":\"ABC\", \"behandlingsGrunnlag\":\"hjemmel1\", \"uthentingsTidspunkt\":\"2019-08-16T12:24:21.675\", \"leverteData\":\""
                +lesFraFil("c:/temp/filRettOver100KEncoded.txt")
                +"\"}";
	}

	private String lesFraFil(String f) {
		try {
			return IOUtils.toString(new FileReader(f));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void doMethod(String method, String resUrl, Map<String, String> headers, String rBody) {
		if (!authSet) {
			addHeader("Authorization: " + getEncodedBasicAuth(SetupServer.brukernavn(), SetupServer.passord()));
		}
		super.doMethod(method, resUrl, getHeaders(), rBody); // mister en substitute() i superklassen her, men vi bruker ikke global variabel-substitusjon uansett
	}

	public void oidctoken() {
		addHeader("Authorization: Bearer "+hentTabellCelleInnhold("oidctoken")); 
		authSet = true;
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
