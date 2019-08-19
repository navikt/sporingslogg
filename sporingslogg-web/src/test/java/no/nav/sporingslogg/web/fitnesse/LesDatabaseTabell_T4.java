package no.nav.sporingslogg.web.fitnesse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LesDatabaseTabell_T4 {
	
	private final String tabellNavn;
	private final String query;

	public LesDatabaseTabell_T4(String tabellNavn, String query) {
		this.tabellNavn = tabellNavn;
		this.query = query;
	}
	
	public List<List<List<String>>> query() {  
		List<List<List<String>>> dbRecords = new ArrayList<List<List<String>>>();
		List<String> kolonneNavnListe = DatabaseDefinisjoner.kolonneNavnForTabell(tabellNavn);
		try {
			Connection conn = connectTilDb();
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM "+tabellNavn+query);
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
		return DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=d26dbfl021.test.local)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=SPORINGSLOGGT4)(INSTANCE_NAME=cctf02)(UR=A)(SERVER=DEDICATED)))", 
				"SPORINGSLOGG_T4", "fcCsL5wDtsOA");
	}
}
