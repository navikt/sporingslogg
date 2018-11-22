package no.nav.sporingslogg.web.fitnesse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;
import no.nav.sporingslogg.standalone.testconfig.UrlUsernamePassword;
import no.nav.sporingslogg.web.fitnesse.DatabaseDefinisjoner.VerdiType;

public class LastDatabaseTabell {
	
	private final String tabellNavn;
	
	public LastDatabaseTabell(String tabellNavn) {
		this.tabellNavn = tabellNavn;
	}
	
    public List<?> doTable(List<List<String>> table) {
	    lastDb(table, true); // TODO styre dette ?
	    return new ArrayList<String>();
    }

	private void lastDb(List<List<String>> table, boolean slettAltForst) {
    	
		try {
	        Connection connection = connectTilDb();
	        if (slettAltForst) {
	        	connection.prepareStatement("delete from "+tabellNavn).executeUpdate();
	        }
	    	if (!table.isEmpty()) {
	    		List<String> kolonneNavn = table.get(0);
	        	List<VerdiType> kolonneTyper = DatabaseDefinisjoner.kolonneTyper(tabellNavn, kolonneNavn);
	            String insertSql = "INSERT INTO "+tabellNavn+" ("+kommaSeparer(kolonneNavn)+") VALUES ("+placeholders(kolonneNavn.size())+")";
		        for (int i=1; i < table.size(); i++) {
					insert(table.get(i), connection, insertSql, kolonneTyper);
				}
	    	}
	        connection.commit();
	        connection.close();
		} catch (Exception e) {
			throw new RuntimeException("Databaselasting feilet", e);
		}
	}

	private String kommaSeparer(List<String> kolonneNavn) {
		StringBuilder sb = new StringBuilder();
		for (String k : kolonneNavn) {
			sb.append(k);
			sb.append(",");
		}
		String s = sb.toString();
		return s.substring(0, s.length()-1);
	}

	private String placeholders(int size) {
		List<String> placeholderList = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			placeholderList.add("?");
		}
		return kommaSeparer(placeholderList);
	}

	private void insert(List<String> kolonneVerdier, Connection connection, String insertSql, List<VerdiType> kolonneTyper) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(insertSql);
        for (int i = 0; i < kolonneVerdier.size(); i++) {
        	String kolonneVerdi = kolonneVerdier.get(i);
        	VerdiType kolonneType = kolonneTyper.get(i);
        	setValue(statement, i+1, kolonneVerdi, kolonneType);
        }
        statement.executeUpdate();
	}

	private void setValue(PreparedStatement statement, int i, String kolonneVerdi, VerdiType kolonneType) throws SQLException {
		switch (kolonneType) {
			case STRING:
				statement.setString(i, kolonneVerdi);
				return;
			case INT:
				statement.setInt(i, Integer.parseInt(kolonneVerdi));
				return;
			case TIMESTAMP:
				statement.setTimestamp(i, timestamp(kolonneVerdi));
				return;
			default:
		}
	}
	
	private Connection connectTilDb() throws SQLException {
		UrlUsernamePassword dbConnection = StandaloneTestJettyMain.HSQL_SERVER;
		return DriverManager.getConnection(dbConnection.getUrl(), dbConnection.getUsername(), dbConnection.getPassword());
	}

	private Timestamp timestamp(String string) {
		LocalDateTime localDateTime = LocalDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
		return Timestamp.valueOf(localDateTime);
	}
}
