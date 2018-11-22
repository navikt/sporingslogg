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

public class ManualKafkaSender { // Send melding(er) til kafka startet via EmbeddedKafkaMain, på LoggMelding JSON format: {"person":"...", "mottaker":"...", "leverteData":"..."}
	
	static final String SERVER_EMBEDDED = "127.0.0.1:9092";  // SETT DENNE SOM EmbeddedKafkaMain SIER
	
	static final String SERVER_TEST = "d26apvl00159.test.local:8443";
	static final String TOPIC_TEST = "sporingslogg";
	
	public static void main(String[] args) {
		Map<String, Object> senderProps = getSenderPropsForTest();		
		new ManualKafkaSender().sendMessages(senderProps, SERVER_TEST, TOPIC_TEST);
	}
	
	public static Map<String, Object> getSenderPropsForEmbeddedKafka() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_EMBEDDED);		
		return senderProps;
	}
	public static Map<String, Object> getSenderPropsForTest() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER_TEST);		
		senderProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		senderProps.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='srvABACPEP' password='hUK1.30sKhqp0.(2';");
		senderProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		senderProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "W:/workspace/sporingslogg/kafkaclient/src/test/resources/nav_truststore_nonproduction_ny2.jts");
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  "467792be15c4a8807681fd2d5c9c1748");
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
//			producer.send(lagKafkaRecord(counter++, topic)).get();
			producer.send(lagTestKafkaRecord(counter++, "srvABACPEP", topic)).get();
		} catch (Exception e) {
			throw new RuntimeException("Kunne ikke sende melding", e);
		} finally {
			producer.close();
		}
		System.out.println("################################### "+(counter-1)+" melding(er) sendt, avslutter");	

	}
	private ProducerRecord<Integer, String> lagTestKafkaRecord(int c, String person, String topic) {
		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding(person,"mott.Org","ABC","hjemmelbeskrivelsen",LocalDateTime.now(),"encoded leverte data","samtykket"));
	}

	private ProducerRecord<Integer, String> lagKafkaRecord(int c, String topic) {
//		String data = "{ \"prop1\":\"value1\", \"prop2\":\"value2\", \"prop3\": {\"innerProp\":5} }";
		String dataEsc = "{ 'prop1':'value1', 'prop2':'value2', 'prop3': {'innerProp':5} }";
		String data = "something";
//		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("person"+c,"org"+c,"tm"+c,"hjemmel"+c,LocalDateTime.now(),"{\\\"prop1\\\":\\\"verdi1\\\"}"));
		return new ProducerRecord<Integer, String>(topic, c, lagLoggMelding("person"+c,"org"+c,"tm"+c,"hjemmel"+c,LocalDateTime.now(),data,"token"+c));
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
