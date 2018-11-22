package no.nav.sporingslogg.standalone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class StandaloneJettyMain {

    static {
    	System.setProperty("logback.configurationFile", StandaloneJettyServer.class.getResource("/webapp/WEB-INF/logback-nais.xml").toString());
    }
    private static final Logger log = LoggerFactory.getLogger(StandaloneJettyMain.class);

    private static final String WEB_XML = "/webapp/WEB-INF/web.xml";

    public static void main(String[] args) throws Exception{
        
    	System.setProperty(PropertyNames.PROPERTY_DB_DIALECT, "org.hibernate.dialect.Oracle10gDialect");
    	System.setProperty(PropertyNames.PROPERTY_DB_SHOWSQL, "false");
    	System.setProperty(PropertyNames.PROPERTY_DB_GENERATEDDL, "false");

        try {
        	StandaloneJettyServer jettyServer = new StandaloneJettyServer(WEB_XML);
        	jettyServer.createContextHandler(createOracleDatasource());
            jettyServer.startJetty(true);
        } catch (Exception e) {
            throw new Exception("Exception when running Jetty Embedded Server setup: " + e);
        }
    }
	
    public static DriverManagerDataSource createOracleDatasource() { 
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        String url = PropertyUtil.getProperty(PropertyNames.PROPERTY_DB_URL);
        ds.setUrl(url);
    	log.info("Jetty satt opp med Oracle, url: " + url);
        ds.setUsername(PropertyUtil.getProperty(PropertyNames.PROPERTY_DB_USERNAME));
        ds.setPassword(PropertyUtil.getProperty(PropertyNames.PROPERTY_DB_PASSWORD));
        return ds;
    }
}
