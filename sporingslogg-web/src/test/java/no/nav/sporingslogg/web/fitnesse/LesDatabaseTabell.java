package no.nav.sporingslogg.web.fitnesse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;
import no.nav.sporingslogg.standalone.testconfig.UrlUsernamePassword;

public class LesDatabaseTabell {
	
	private final String tabellNavn;
	private final String query;
	
	public LesDatabaseTabell(String tabellNavn, String query) { // TODO query senere....
		this.tabellNavn = tabellNavn;
		this.query = query;
	}
	
	public List<List<List<String>>> query() {  
		List<List<List<String>>> dbRecords = new ArrayList<List<List<String>>>();
		List<String> kolonneNavnListe = DatabaseDefinisjoner.kolonneNavnForTabell(tabellNavn);
		try {
			Connection conn = connectTilDb();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM "+tabellNavn);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				List<List<String>> dbRecord = new ArrayList<List<String>>();
				for (String kolonneNavn : kolonneNavnListe) {
					dbRecord.add(keyValueList(kolonneNavn, rs));							
				}
				dbRecords.add(dbRecord);
			}
			conn.close();
		} catch (Exception e) {
			System.out.println("Got exception: " + e.getMessage());
		}
		return dbRecords;
	}

	private List<String> keyValueList(String key, ResultSet rs) throws SQLException {
		return Arrays.asList(key, rs.getString(key));
	}
	
	private Connection connectTilDb() throws SQLException {
		UrlUsernamePassword dbConnection = StandaloneTestJettyMain.HSQL_SERVER;
		return DriverManager.getConnection(dbConnection.getUrl(), dbConnection.getUsername(), dbConnection.getPassword());
	}
}
