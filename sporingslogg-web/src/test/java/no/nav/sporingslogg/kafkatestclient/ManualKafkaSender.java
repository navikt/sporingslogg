package no.nav.sporingslogg.kafkatestclient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import no.nav.sporingslogg.kafka.KafkaProperties;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;

public class ManualKafkaSender { // Send melding(er) til kafka startet via EmbeddedKafkaMain, på LoggMelding JSON format: {"person":"...", "mottaker":"...", "leverteData":"..."}
	
	
	private static final KafkaProperties EMBEDDED_PROPERTIES = StandaloneTestJettyMain.getKafkaEmbeddedProperties();
	private static final KafkaProperties TEST_PROPERTIES = StandaloneTestJettyMain.getKafkaTestProperties();
	private static final KafkaProperties PREPROD_PROPERTIES = StandaloneTestJettyMain.getKafkaPreprodProperties();

	public static void main(String[] args) {
		Map<String, Object> senderProps = getSenderPropsForTest();		
		new ManualKafkaSender().sendMessages(senderProps, TEST_PROPERTIES.getBootstrapServers(), TEST_PROPERTIES.getTopic());
//		Map<String, Object> senderProps = getSenderPropsForPreprod();		
//		new ManualKafkaSender().sendMessages(senderProps, PREPROD_PROPERTIES.getBootstrapServers(), PREPROD_PROPERTIES.getTopic());
//		Map<String, Object> senderProps = getSenderPropsForEmbeddedKafka();		
//		new ManualKafkaSender().sendMessages(senderProps, EMBEDDED_PROPERTIES.getBootstrapServers(), EMBEDDED_PROPERTIES.getTopic());
	}
	
	public static Map<String, Object> getSenderPropsForEmbeddedKafka() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, EMBEDDED_PROPERTIES.getBootstrapServers());		
		return senderProps;
	}
	public static Map<String, Object> getSenderPropsForTest() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, TEST_PROPERTIES.getBootstrapServers());		
		senderProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		senderProps.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+TEST_PROPERTIES.getUsername()
		+"' password='"+TEST_PROPERTIES.getPassword()+"';");
		senderProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		senderProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, TEST_PROPERTIES.getTruststoreFile());
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  TEST_PROPERTIES.getTruststorePassword());
		return senderProps;
	}
	public static Map<String, Object> getSenderPropsForPreprod() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PREPROD_PROPERTIES.getBootstrapServers());		
		senderProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		senderProps.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+PREPROD_PROPERTIES.getUsername()
		+"' password='"+PREPROD_PROPERTIES.getPassword()+"';");
		senderProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		senderProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, PREPROD_PROPERTIES.getTruststoreFile());
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  PREPROD_PROPERTIES.getTruststorePassword());
		return senderProps;
	}
	public static Map<String, Object> getGeneralSenderProps() {
		Map<String, Object> senderProps = new HashMap<>();
		senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return senderProps;
	}
	public void sendMessages(Map<String, Object> senderProps, String server, String topic) {		
		System.out.println("################################### Oppretter producer mot server: " + server);		
		KafkaProducer<Integer, String> producer = new KafkaProducer<>(senderProps);		
		
		System.out.println("################################### sender meldinger ");		
		int counter = 1;
		try {
//			producer.send(lagKafkaRecord(counter++, topic)).get();
//			producer.send(lagKafkaRecord(counter++, topic)).get();
//			producer.send(lagKafkaRecord(counter++, topic)).get();
			producer.send(lagKafkaRecord(counter++, topic)).get();
			
			//producer.send(hardcodedRecord(counter, topic, "Ikke JSON i det hele tatt")); // JsonSyntaxException
			//producer.send(hardcodedRecord(counter, topic, "{ ugyldig json }")); // JsonSyntaxException
			//producer.send(hardcodedRecord(counter, topic, "{ \"person\":\"mye_mangler\" }")); // IllegalArgumentException fra ValideringsTjeneste
			
		} catch (Exception e) {
			throw new RuntimeException("Kunne ikke sende melding", e);
		} finally {
			producer.close();
		}
		System.out.println("################################### "+(counter-1)+" melding(er) sendt, avslutter");	

	}

	private ProducerRecord<Integer, String> hardcodedRecord(int c, String topic, String melding) {
		return new ProducerRecord<Integer, String>(topic, c, melding);
	}

	private ProducerRecord<Integer, String> lagKafkaRecord(int c, String topic) {
//		String data = "{ \"prop1\":\"value1\", \"prop2\":\"value2\", \"prop3\": {\"innerProp\":5} }";
		String dataEsc = "{ 'prop1':'value1', 'prop2':'value2', 'prop3': {'innerProp':5} }";
		String data = "something";
//		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("person"+c,"org"+c,"tm"+c,"hjemmel"+c,LocalDateTime.now(),"{\\\"prop1\\\":\\\"verdi1\\\"}"));
		// OK melding
//		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("person"+c,"org"+c,"tm"+c,"hjemmel"+c,LocalDateTime.now(),data,"token"+c));
		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("person"+c,"0192:987654321","UFO","hjemmel"+c,LocalDateTime.now(),data,"token"+c));
		// serialiseringsfeil
//		return new ProducerRecord<Integer, String>(topic, c, "tullball");
		// valideringsfeil
//		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("person"+c,"","tm"+c,"hjemmel"+c,LocalDateTime.now(),data,"token"+c));
		// simulert DB-exception (må kodes inn i KafkaLoggConsumer: exception kastes for person = "skalFeile")
//		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("skalFeile","org"+c,"tm"+c,"hjemmel"+c,LocalDateTime.now(),data,"token"+c));
	}
/*
 *     private String person;                       // Fnr/dnr for personen dataene gjelder
    private String mottaker;                     // Orgnr som dataene leveres ut til
    private String tema;                         // Type data, som definert i https://modapp.adeo.no/kodeverksklient/viskodeverk, Tema
    private String hjemmel;						 // Beskriver hjemmel som er bakgrunn for at dataene utleveres TODO kodeverk e.l.
    private ZonedDateTime uthentingsTidspunkt;   // Tidspunkt for utlevering
    private String leverteData;                  // Utleverte data

 */
	private String lagLoggMelding(String p, String m, String t, String h, LocalDateTime u, String d, String token) {
	    return "{" 
	    		+ jsonProperty("person", p) + "," 
	    		+ jsonProperty("mottaker", m) + "," 
	    		+ jsonProperty("tema", t) + "," 
	    		+ jsonProperty("behandlingsGrunnlag", h) + "," 
	    		+ jsonProperty("uthentingsTidspunkt", u.format(DateTimeFormatter.ISO_DATE_TIME)) + "," 
//	    		+ inApo("leverteData")+":"+d  // som json object, da må LoggMelding.leverteData ha type Object
	    		+ jsonProperty("leverteData", d)  + "," // som string (må være encodet)
	    		+ jsonProperty("samtykkeToken", token)
	    		+ "}";
	}
	
	private String jsonProperty(String name, String value) {
		return inApo(name) + ":" + inApo(value);
	}
	private String inApo(String s) {
		return "\""+s+"\"";
	}
}
