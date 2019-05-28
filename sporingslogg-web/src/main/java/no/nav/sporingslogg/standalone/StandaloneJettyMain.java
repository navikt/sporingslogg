package no.nav.sporingslogg.standalone;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneJettyMain {

    static {
    	System.setProperty("logback.configurationFile", StandaloneJettyServer.class.getResource("/webapp/WEB-INF/logback-nais.xml").toString());
    }
    private static final Logger log = LoggerFactory.getLogger(StandaloneJettyMain.class);

    private static final String WEB_XML = "/webapp/WEB-INF/web.xml";

    public static void main(String[] args) throws Exception {
        
    	// Fiks properties så de får korrekte navn, og sett de som evt ikke er satt i env
    	
    	PropertyUtil.fixDockerEnvProblem();
    	
		String passwordFile = System.getenv("PASSWORD_FILE");
		Properties passwords = readFromSecretsPropertyFile(passwordFile);
		
		System.setProperty(PropertyNames.PROPERTY_LDAP_PASSWORD, passwords.getProperty("LDAP_PASSWORD"));
		System.setProperty(PropertyNames.PROPERTY_DB_PASSWORD, passwords.getProperty("SPORINGSLOGGDB_PASSWORD"));
		System.setProperty(PropertyNames.PROPERTY_KAFKA_PASSWORD, passwords.getProperty("NO_NAV_SPORINGSLOGG_KAFKA_PASSWORD"));
		
    	System.setProperty(PropertyNames.PROPERTY_DB_DIALECT, "org.hibernate.dialect.Oracle10gDialect");
    	System.setProperty(PropertyNames.PROPERTY_DB_SHOWSQL, "false");
    	System.setProperty(PropertyNames.PROPERTY_DB_GENERATEDDL, "false");

    	System.setProperty(PropertyNames.PROPERTY_KAFKA_TRUSTSTORE_FILE, System.getenv("NAV_TRUSTSTORE_PATH"));
    	System.setProperty(PropertyNames.PROPERTY_KAFKA_TRUSTSTORE_PASSWORD, System.getenv("NAV_TRUSTSTORE_PASSWORD"));
    	
    	String kafkaGroupId = System.getenv("NO_NAV_SPORINGSLOGG_KAFKA_GROUPID");
    	String kafkaProducerGroupId = System.getenv("NO_NAV_SPORINGSLOGG_KAFKA_PRODUCERGROUPID");
    	if (kafkaGroupId == null) {
    		kafkaGroupId = "KC-"+System.getenv("NO_NAV_SPORINGSLOGG_KAFKA_TOPIC");
    		log.info("Kafka consumer group not explicitly set, using topic name to set group name: ");
    		System.setProperty(PropertyNames.PROPERTY_KAFKA_GROUP, kafkaGroupId);
    	}
    	if (kafkaProducerGroupId == null) {
    		kafkaProducerGroupId = "KP-"+System.getenv("NO_NAV_SPORINGSLOGG_KAFKA_TOPIC");
    		log.info("Kafka producer group not explicitly set, using topic name to set group name: ");
    		System.setProperty(PropertyNames.PROPERTY_KAFKA_PRODUCERGROUP, kafkaProducerGroupId);
    	}

    	// Start serveren
    	
    	try {
        	StandaloneJettyServer jettyServer = new StandaloneJettyServer(WEB_XML);
        	jettyServer.createContextHandler(createOracleDatasource());
            jettyServer.startJetty(true);
        } catch (Exception e) {
            throw new Exception("Exception when running Jetty Embedded Server setup: " + e);
        }
    }
	
	private static Properties readFromSecretsPropertyFile(String filename) {
		try (FileInputStream f = new FileInputStream(filename)) {
			Properties secrets = new Properties();
			secrets.load(f);
			return secrets;
		} catch (IOException e) {
			throw new RuntimeException("Kan ikke lese secrets fil "+filename+" fra vault", e);
		}
	}

    public static DataSource createOracleDatasource() { 
    	
        String url = PropertyUtil.getProperty(PropertyNames.PROPERTY_DB_URL);
        String username = PropertyUtil.getProperty(PropertyNames.PROPERTY_DB_USERNAME);
        String pw = PropertyUtil.getProperty(PropertyNames.PROPERTY_DB_PASSWORD);
        return createOracleDatasource(url, username, pw);
    }
    
    public static DataSource createOracleDatasource(String url, String username, String pw) {  // brukes også fra testversjon
    	
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, username, pw);
                PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
                
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        // Sett konfig for pool her med poolConfig.setXXX
        String params = "";
        params += "-- max total: " + poolConfig.getMaxTotal() + "\n";
        params += "-- max idle: " + poolConfig.getMaxIdle() + "\n";
        params += "-- min idle: " + poolConfig.getMinIdle() + "\n";
        params += "-- max wait millis: " + poolConfig.getMaxWaitMillis() + "\n";
        params += "-- block when exhausted: " + poolConfig.getBlockWhenExhausted() + "\n";
        params += "-- getEvictionPolicyClassName: " + poolConfig.getEvictionPolicyClassName() + "\n";
        params += "-- getTimeBetweenEvictionRunsMillis: " + poolConfig.getTimeBetweenEvictionRunsMillis() + "\n";
        params += "-- getMinEvictableIdleTimeMillis: " + poolConfig.getMinEvictableIdleTimeMillis() + "\n";
        params += "-- getSoftMinEvictableIdleTimeMillis: " + poolConfig.getSoftMinEvictableIdleTimeMillis() + "\n";
        params += "-- getNumTestsPerEvictionRun: " + poolConfig.getNumTestsPerEvictionRun() + "\n";
        params += "-- getEvictorShutdownTimeoutMillis: " + poolConfig.getEvictorShutdownTimeoutMillis() + "\n";
        log.info("DB conn pool satt opp med disse parametrene: \n" + params);
        
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory, poolConfig);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        
    	log.info("Jetty satt opp med DB, url: " + url);
        return dataSource;
    }
}
