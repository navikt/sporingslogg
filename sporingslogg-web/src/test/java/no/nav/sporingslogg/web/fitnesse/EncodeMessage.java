package no.nav.sporingslogg.web.fitnesse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class EncodeMessage {
	
	private final String melding;

	public EncodeMessage(String melding) {
		this.melding = melding;
	}
	
	public List<List<List<String>>> query() {  
		String meldingMedTimestamp = melding + " " +LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
		String encoded = Base64.getEncoder().encodeToString(meldingMedTimestamp.getBytes());
		
		List<List<String>> tableRecord = new ArrayList<List<String>>();
		tableRecord.add(Arrays.asList("Dummykey", "1"));							
		tableRecord.add(Arrays.asList("Melding", meldingMedTimestamp));							
		tableRecord.add(Arrays.asList("Encodedstring", encoded));			
		
		List<List<List<String>>> table = new ArrayList<List<List<String>>>();
		table.add(tableRecord);
		return table;
	}
	
	// For å generere filer med encoded payload i (til test av store meldinger)
//	public static void main(String[] args) throws IOException {
//		
//		byte[] x = IOUtils.toByteArray(new FileInputStream("c:/temp/inpuEncode1.txt"));
//		String encoded = Base64.getEncoder().encodeToString(x);
//		System.out.println("Encoded length: " + encoded.length());
//		IOUtils.write(encoded, new FileWriter("c:/temp/filRettOver100KEncoded.txt"));
//	}
}
