package no.nav.sporingslogg.kafka;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import no.nav.sporingslogg.domain.LoggInnslag;
import no.nav.sporingslogg.restapi.LoggMelding;
import no.nav.sporingslogg.tjeneste.LoggTjeneste;

@Ignore
public class TestWithKafka {  // Disse testene er veldig vaklete, virker ca annenhver gang... ikke lurt å kjøre dem automatisk

	private static final String TOPIC = "messages";
	
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TOPIC);
	
	@Test
	@Ignore
	public void produceAndConsumeWithPlainKafkaConsumer() throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
		KafkaProducer<Integer, String> producer = new KafkaProducer<>(senderProps);
		producer.send(new ProducerRecord<>(TOPIC, 0, "message0")).get();		
		producer.send(new ProducerRecord<>(TOPIC, 1, "message1")).get();
		producer.send(new ProducerRecord<>(TOPIC, 2, "message2")).get();
		producer.send(new ProducerRecord<>(TOPIC, 3, "message3")).get();     
		System.out.println("################################### sent");
		
		Thread.sleep(1000);
		System.out.println("################################### slept");
		
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("sampleRawConsumer", "false", embeddedKafka);
		consumerProps.put("auto.offset.reset", "earliest");   
		
		final CountDownLatch latch = new CountDownLatch(4);
		List<String> received = new ArrayList<>();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		System.out.println("################################### ready to read");
		executorService.execute(() -> {    
			KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(consumerProps);
			kafkaConsumer.subscribe(Collections.singletonList(TOPIC));    
			try {       
				while (true) {            
					ConsumerRecords<Integer, String> records = kafkaConsumer.poll(100);            
					for (ConsumerRecord<Integer, String> record : records) {                
						System.out.println("-----------------------------------consuming from topic, partition, offset, key, value " + record.topic()+" "+record.partition()+" "+record.offset()+" "+record.key()+" "+record.value());
						received.add(record.value());
						latch.countDown();            
					}        
				}   
			} finally {        
				kafkaConsumer.close();    
			}
		});           
		latch.await(5, TimeUnit.SECONDS);
		assertTrue(received.contains("message0"));
		assertTrue(received.contains("message1"));
		assertTrue(received.contains("message2"));
		assertTrue(received.contains("message3"));
	}
	
	@Test
	@Ignore
	public void produceAndConsumeWithKafkaLoggConsumer() throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
		senderProps.put("key.serializer", IntegerSerializer.class);
		senderProps.put("value.serializer", StringSerializer.class);
		KafkaProducer<Integer, String> producer = new KafkaProducer<>(senderProps);

		String server = embeddedKafka.getBrokersAsString();
		KafkaProperties kp = new KafkaProperties();
		kp.setTopic(TOPIC);
		kp.setGroupId("sampleRawConsumer");
		kp.setBootstrapServers(server);
		KafkaLoggConsumer consumer = new KafkaLoggConsumer(kp);
		List<String> received = new ArrayList<>();
		consumer.loggTjeneste = new MyLoggTjeneste(received);
		System.out.println("################################### ready to read");
		
		Thread.sleep(1000);
		System.out.println("################################### slept after ready");
		
		producer.send(new ProducerRecord<Integer, String>(TOPIC, 0, lagLoggMelding("person1","org1","data1"))).get();		
		producer.send(new ProducerRecord<Integer, String>(TOPIC, 1, lagLoggMelding("person2","org2","data2"))).get();		
		producer.send(new ProducerRecord<Integer, String>(TOPIC, 2, lagLoggMelding("person3","org3","data3"))).get();		
		producer.send(new ProducerRecord<Integer, String>(TOPIC, 3, lagLoggMelding("person4","org4","data4"))).get();		
		System.out.println("################################### sent");
		
		Thread.sleep(1000);		
		System.out.println("################################### slept after sent");

		assertTrue("r1", received.contains(concatString("person1","org1","data1")));
		assertTrue("r2", received.contains(concatString("person2","org2","data2")));
		assertTrue("r3", received.contains(concatString("person3","org3","data3")));
		assertTrue("r4", received.contains(concatString("person4","org4","data4")));
	}

	private String lagLoggMelding(String p, String o, String d) {
		LoggMelding l = new LoggMelding();
		l.setPerson(p);
		l.setMottaker(o);
		l.setLeverteData(d);
		l.setBehandlingsGrunnlag("behGrunnlag");
		l.setId("ID");
		l.setSamtykkeToken("tokenet");
		l.setTema("XYZ");
		l.setUthentingsTidspunkt(LocalDateTime.now());
		l.setDataForespørsel("my request");
		l.setLeverandør("987654321");
	    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {		
		    @Override
			public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
		    	return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME));
			}
		}).create();
		return gson.toJson(l);
	}

	static String concatString(String person, String org, String data) {
		return person+"-"+org+"-"+data;
	}		

	static class MyLoggTjeneste extends LoggTjeneste {

		private final List<String> received;
		
		public MyLoggTjeneste(List<String> received) {
			this.received = received;
		}

		@Override
		public Long lagreLoggInnslag(LoggInnslag l) {
			received.add(concatString(l.getPerson(), l.getMottaker(), l.getLeverteData()));
			System.out.println(l);
			return null;
		}
	}
}
