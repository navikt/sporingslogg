package no.nav.sporingslogg.standalone.testconfig;

import java.io.IOException;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.standalone.PropertyNames;
import no.nav.sporingslogg.standalone.StandaloneJettyServer;

public class StandaloneTestJettyMain {

	/*
	 * Denne test-jettyen kan kjøre med dummy configs for login-LDAP og OIDC-issuer, eller testversjoner, sjekk properties lenger ned.
	 * Den kan også kjøre med dummy kafka (poller ikke), embedded (må starte EmbeddedKafkaMain i tillegg), eller test.local, sjekk properties.
	 * ManualKafkaSender kan brukes til å logge til kø, både embedded og test.local.
	 * Kan kjøre med HSQL eller Oracle i T4, se properties.
	 */
	// SETT DENNE HVIS KAFKA test.local brukes, SIDEN TRUSTSTORE FOR DENNE MÅ HA ABSOLUTT PATH
	private static final String SPORINGSLOGG_ROOT = "W:/workspace/sporingslogg";
	
    static {
    	System.setProperty("logback.configurationFile", StandaloneTestJettyMain.class.getResource("/webapp/WEB-INF/local-jetty/logback-localtest.xml").toString());
    }
    private static final Logger log = LoggerFactory.getLogger(StandaloneTestJettyMain.class);

	// Denne web.xml'en legger til liste av appcontexts angitt i system-properties 'xxxContextOverrides' (settes under)
    private static final String WEB_XML = "/webapp/WEB-INF/local-jetty/jettyWebWithLocalOverrides.xml";

    private static final int JETTY_PORT_STANDALONE_TEST = 9098;
    
    // Mulige configs for OIDC-autentisering (for lesing via REST)
    public enum OidcConfig {
    	DUMMY,           // ingen auth, alt aksepteres
    	TEST_RESTSTS, 
    	TEST_AZURE;  // TODO ikke impl ennå
    }
    
    // Mulige configs for LDAP-autentisering ved logging gjennom REST (ikke Kafka)
    public enum LoginConfig {
    	DUMMY,          // ingen auth, alt aksepteres
    	TEST_LDAP;
    }
    
    // Mulige configs for Kafka-input
    public enum KafkaConfig {
    	DUMMY,          // leser ikke fra Kafka
    	EMBEDDED,
    	TEST_LOCAL;     // Må ha topic satt opp i test-local, se under for properties
    }
    
    // Mulige configs for database
	public enum DbConfig {
		ORACLE,               // Lokal Oracle
		HSQL_INPROCESS,       // HSQL in-memory som bare kan aksesseres fra denne VM'en
		HSQL_SERVER;          // HSQL in-memory som kan aksesseres fra andre prosesser på denne maskinen (kan lese/skrive fra Fitnesse eller SQLDeveloper)
	}
    
    /*
     * Kan kjøres som selvstendig JAR, for å emulere kjøring på NAIS:
     * 
     *  java -cp sporingslogg-web/target/sporingslogg-web.jar no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain
     *  
     * Parametre hvis ikke defaultverdier skal brukes, f.eks: oidcconfig=test_reststs loginconfig=test_ldap
     *  
     */
    public static void main(String[] args) throws Exception {
    	
    	// Bruk parametre, eller default-verdier hvis ikke gitt
    	OidcConfig oidcConfig =   useParam(args, "oidcConfig") != null ?  OidcConfig.valueOf(useParam(args, "oidcConfig").toUpperCase()) :   OidcConfig.DUMMY;
    	KafkaConfig kafkaConfig = useParam(args, "kafkaConfig") != null ? KafkaConfig.valueOf(useParam(args, "kafkaConfig").toUpperCase()) : KafkaConfig.DUMMY;
    	LoginConfig loginConfig = useParam(args, "loginConfig") != null ? LoginConfig.valueOf(useParam(args, "loginConfig").toUpperCase()) : LoginConfig.DUMMY;
    	DbConfig dbConfig =       useParam(args, "dbConfig") != null ?    DbConfig.valueOf(useParam(args, "dbConfig").toUpperCase()) :       DbConfig.HSQL_INPROCESS;
    	
    	System.out.println("Starter Sporingslogg TEST med OIDCCONFIG "+oidcConfig+", LOGINCONFIG "+loginConfig+", KAFKACONFIG "+kafkaConfig+", DBCONFIG "+dbConfig);    	

        try {
        	StandaloneTestJettyMain jettyMain = new StandaloneTestJettyMain(oidcConfig, kafkaConfig, loginConfig, dbConfig, JETTY_PORT_STANDALONE_TEST, true);
        } catch (Exception e) {
            throw new Exception("Exception when running Jetty Embedded Server setup: " + e);
        }
    }

    private static String useParam(String[] args, String arg) {
		for (String s : args) {
			if (s.toLowerCase().startsWith(arg.toLowerCase()+"=")) {
				return s.substring(arg.length()+1);
			}
		}
		return null;
	}

	// Med kjøring fra Fitnesse skal vi IKKE joine server-prosessen, den skal gå separat og polle pga Kafka
    public StandaloneTestJettyMain(OidcConfig oidcConfig, KafkaConfig kafkaConfig, LoginConfig loginConfig, DbConfig dbConfig, int port, boolean rejoin) throws Exception {
		setupOidc(oidcConfig);
		setupKafka(kafkaConfig);
		setupLoginAuth(loginConfig);
		setupDb(dbConfig);
		
    	StandaloneJettyServer jettyServer = new StandaloneJettyServer(WEB_XML, port);
    	jettyServer.createContextHandler(dataSource);
        jettyServer.startJetty(rejoin);
	}

	private void setupDb(DbConfig dbConfig) {
		switch (dbConfig) {
		case ORACLE:
	    	System.setProperty(PropertyNames.PROPERTY_DB_DIALECT, "org.hibernate.dialect.Oracle10gDialect");
	    	System.setProperty(PropertyNames.PROPERTY_DB_SHOWSQL, "true");
	    	System.setProperty(PropertyNames.PROPERTY_DB_GENERATEDDL, "false");
			dataSource = createOracleTestDatasource();
			return;
		case HSQL_INPROCESS:
	    	System.setProperty(PropertyNames.PROPERTY_DB_DIALECT, "org.hibernate.dialect.HSQLDialect");
	    	System.setProperty(PropertyNames.PROPERTY_DB_SHOWSQL, "true");
	    	System.setProperty(PropertyNames.PROPERTY_DB_GENERATEDDL, "true");
	    	dataSource = createHsqlDatasource();
			return;
		case HSQL_SERVER:
	    	System.setProperty(PropertyNames.PROPERTY_DB_DIALECT, "org.hibernate.dialect.HSQLDialect");
	    	System.setProperty(PropertyNames.PROPERTY_DB_SHOWSQL, "true");
	    	System.setProperty(PropertyNames.PROPERTY_DB_GENERATEDDL, "true");
	    	dataSource = createHsqlServerDatasource();
	    	startHsqlServer();
		}
	}


	private void setupLoginAuth(LoginConfig loginConfig) {
		switch (loginConfig) {
		case TEST_LDAP:
			System.setProperty("loginContextOverrides", "/WEB-INF/local-jetty/noContextOverrides.xml");
	        System.setProperty(PropertyNames.PROPERTY_LDAP_URL, "ldap://ldapgw.test.local:389");
	        System.setProperty(PropertyNames.PROPERTY_LDAP_USERNAME, "srvSSOLinux");
	        System.setProperty(PropertyNames.PROPERTY_LDAP_PASSWORD, "khFKL0GCusWe72");
	        System.setProperty(PropertyNames.PROPERTY_LDAP_BASEDN, "dc=test,dc=local");
	        System.setProperty(PropertyNames.PROPERTY_LDAP_USER_BASEDN, "ou=NAV,ou=BusinessUnits,dc=test,dc=local");
	        System.setProperty(PropertyNames.PROPERTY_LDAP_SERVICEUSER_BASEDN, "ou=ServiceAccounts,dc=test,dc=local");
			return;
		case DUMMY:
			System.setProperty("loginContextOverrides", "/WEB-INF/local-jetty/dummyLoginContextOverrides.xml");
	        setPropertyNotUsed(PropertyNames.PROPERTY_LDAP_URL);
	        setPropertyNotUsed(PropertyNames.PROPERTY_LDAP_USERNAME);
	        setPropertyNotUsed(PropertyNames.PROPERTY_LDAP_PASSWORD);
	        setPropertyNotUsed(PropertyNames.PROPERTY_LDAP_BASEDN);
	        setPropertyNotUsed(PropertyNames.PROPERTY_LDAP_USER_BASEDN);
	        setPropertyNotUsed(PropertyNames.PROPERTY_LDAP_SERVICEUSER_BASEDN);
		}
	}


	private void setupKafka(KafkaConfig kafkaConfig) {
		switch (kafkaConfig) {
		case TEST_LOCAL:
			System.setProperty("kafkaContextOverrides", "/WEB-INF/local-jetty/noContextOverrides.xml"); 
			KafkaProperties kp = getKafkaTestProperties();
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_TOPIC, kp.getTopic());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_GROUP, kp.getGroupId());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_SERVER, kp.getBootstrapServers());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_USERNAME, kp.getUsername());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_PASSWORD, kp.getPassword());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_TRUSTSTORE_FILE, kp.getTruststoreFile());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_TRUSTSTORE_PASSWORD, kp.getTruststorePassword());
			return;
		case EMBEDDED:
			System.setProperty("kafkaContextOverrides", "/WEB-INF/local-jetty/embeddedKafkaContextOverrides.xml"); 
			KafkaProperties kp2 = getKafkaEmbeddedProperties();
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_TOPIC, kp2.getTopic());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_GROUP, kp2.getGroupId());
	        System.setProperty(PropertyNames.PROPERTY_KAFKA_SERVER, kp2.getBootstrapServers());
			return;
		case DUMMY:
			System.setProperty("kafkaContextOverrides", "/WEB-INF/local-jetty/dummyKafkaContextOverrides.xml"); 
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_TOPIC);
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_GROUP);
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_SERVER);
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_USERNAME);
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_PASSWORD);
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_TRUSTSTORE_FILE);
	        setPropertyNotUsed(PropertyNames.PROPERTY_KAFKA_TRUSTSTORE_PASSWORD);
		}
	}
/*
Denne ble opprettet med følgende JSON til oneShot-tjenesten, av bruker k148586 (Aage K):
{
  "topics": [
    {
      "configEntries": {},
      "members": [
        {
          "member": "srvsporingslogg",
          "role": "PRODUCER"
        },
        {
          "member": "srvsporingslogg",
          "role": "CONSUMER"
        }
      ],
      "numPartitions": 1,
      "topicName": "aapen-sporingslogg-loggmeldingMottatt"
    }
  ]
} */
	public static KafkaProperties getKafkaTestProperties() {
		KafkaProperties kp = new KafkaProperties();
		kp.setBootstrapServers("d26apvl00159.test.local:8443,d26apvl00160.test.local:8443,d26apvl00161.test.local:8443");
		kp.setTopic("aapen-sporingslogg-loggmeldingMottatt");
		kp.setGroupId("KC-aapen-sporingslogg-loggmeldingMottatt");
		kp.setUsername("srvsporingslogg");
		kp.setPassword("EbLAPrQYvY1JrLS");
        kp.setTruststoreFile(SPORINGSLOGG_ROOT+"/sporingslogg-web/src/main/webapp/WEB-INF/local-jetty/nav_truststore_nonproduction_ny2.jts");
        kp.setTruststorePassword("467792be15c4a8807681fd2d5c9c1748");
        return kp;
	}

	public static KafkaProperties getKafkaEmbeddedProperties() {
		KafkaProperties kp = new KafkaProperties();
		kp.setBootstrapServers("127.0.0.1:9092");
		kp.setTopic("sporingsLoggTopic");
		kp.setGroupId("sporingsLoggGroupId");
        return kp;
	}

	private void setupOidc(OidcConfig oidcConfig) {
		switch (oidcConfig) {
		case TEST_AZURE:
			throw new RuntimeException("Har ikke implementert OIDC med Azure ennå...");
		case TEST_RESTSTS:
			System.setProperty("oidcContextOverrides", "/WEB-INF/local-jetty/noContextOverrides.xml");
	        SslCertificateBypasser.disableSSLCertificateChecking();
	        System.setProperty(PropertyNames.PROPERTY_OIDC_JWKS, "https://security-token-service.nais.preprod.local/rest/v1/sts/jwks");
	        System.setProperty(PropertyNames.PROPERTY_OIDC_ISSUER, "https://security-token-service.nais.preprod.local");
	        return;
		case DUMMY:
			System.setProperty("oidcContextOverrides", "/WEB-INF/local-jetty/dummyOidcContextOverrides.xml");
	        setPropertyNotUsed(PropertyNames.PROPERTY_OIDC_JWKS);
	        setPropertyNotUsed(PropertyNames.PROPERTY_OIDC_ISSUER);
		}
	}


	private void setPropertyNotUsed(String key) {
		System.setProperty(key, "not_used");
	}

	private DriverManagerDataSource dataSource;
	
	
    DriverManagerDataSource createOracleTestDatasource() { 
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=d26dbfl021.test.local)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=SPORINGSLOGGT4)(INSTANCE_NAME=cctf02)(UR=A)(SERVER=DEDICATED)))");
    	log.info("Jetty satt opp med Oracle, T4");
        ds.setUsername("SPORINGSLOGG_T4");
        ds.setPassword("fcCsL5wDtsOA");
        return ds;
    }
    
	DriverManagerDataSource createHsqlDatasource() {
    	log.info("Jetty satt opp med HSQL");
        DriverManagerDataSource ds = new DriverManagerDataSource();
	    ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:mem:mydb;sql.enforce_strict_size=true");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }
	// Denne brukes av Fitnesse-fixtures som aksesserer DB
	public static final UrlUsernamePassword HSQL_SERVER = new UrlUsernamePassword("jdbc:hsqldb:hsql://localhost/mydb;sql.enforce_strict_size=true", "sa", "");
	DriverManagerDataSource createHsqlServerDatasource() {
    	log.info("Jetty satt opp med HSQL");
        DriverManagerDataSource ds = new DriverManagerDataSource();
	    ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:hsql://localhost/mydb;sql.enforce_strict_size=true");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }
	private void startHsqlServer() {
		System.out.println("Starter HSQL server på port 9001");
		HsqlProperties p = new HsqlProperties();
		p.setProperty("server.database.0", "mem:mydb");
		p.setProperty("server.dbname.0", "mydb");
		p.setProperty("server.port", "9001");
		org.hsqldb.Server server = new org.hsqldb.Server();
		try {
			server.setProperties(p);
		} catch (IOException | AclFormatException e) {
			throw new RuntimeException("Kan ikke starte HSQL server", e);
		}
		server.setLogWriter(null); // can use custom writer
		server.setErrWriter(null); // can use custom writer
		server.start();
		System.out.println("HSQL server startet");
	}
}
