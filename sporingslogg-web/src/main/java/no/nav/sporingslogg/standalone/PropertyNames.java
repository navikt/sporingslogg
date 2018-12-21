package no.nav.sporingslogg.standalone;

public class PropertyNames {
	
	// Definerer de properties som må være satt for at standalone Jetty kan fungere.
	// Må settes ved nais-deploy/Fasit eller i main.
	
    public static final String PROPERTY_OIDC_JWKS = "no.nav.sporingslogg.oidc.jwks";
    public static final String PROPERTY_OIDC_ISSUER = "no.nav.sporingslogg.oidc.issuer";
    
    public static final String PROPERTY_KAFKA_TOPIC = "no.nav.sporingslogg.kafka.topic";
    public static final String PROPERTY_KAFKA_GROUP = "no.nav.sporingslogg.kafka.groupid";
    public static final String PROPERTY_KAFKA_PRODUCERGROUP = "no.nav.sporingslogg.kafka.producergroupid";
    public static final String PROPERTY_KAFKA_SERVER = "no.nav.sporingslogg.kafka.servers";
    public static final String PROPERTY_KAFKA_USERNAME = "no.nav.sporingslogg.kafka.username";
    public static final String PROPERTY_KAFKA_PASSWORD = "no.nav.sporingslogg.kafka.password";
    public static final String PROPERTY_KAFKA_TRUSTSTORE_FILE = "no.nav.sporingslogg.kafka.truststorefile";
    public static final String PROPERTY_KAFKA_TRUSTSTORE_PASSWORD = "no.nav.sporingslogg.kafka.truststorepassword";
    
    public static final String PROPERTY_DB_URL = "sporingsloggDB.url";
    public static final String PROPERTY_DB_USERNAME = "sporingsloggDB.username";
    public static final String PROPERTY_DB_PASSWORD = "sporingsloggDB.password";
	public static final String PROPERTY_DB_DIALECT = "sporingsloggDB.dialect";
	public static final String PROPERTY_DB_SHOWSQL = "sporingsloggDB.showSql";
	public static final String PROPERTY_DB_GENERATEDDL = "sporingsloggDB.generateDdl";
	
    public static final String PROPERTY_LDAP_URL = "ldap.url";
    public static final String PROPERTY_LDAP_USERNAME = "ldap.username";
    public static final String PROPERTY_LDAP_PASSWORD = "ldap.password";
    public static final String PROPERTY_LDAP_BASEDN = "ldap.basedn";
    public static final String PROPERTY_LDAP_USER_BASEDN = "ldap.user.basedn";
    public static final String PROPERTY_LDAP_SERVICEUSER_BASEDN = "ldap.serviceuser.basedn";

}
