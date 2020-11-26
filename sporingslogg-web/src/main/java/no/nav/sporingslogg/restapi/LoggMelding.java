package no.nav.sporingslogg.restapi;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class LoggMelding { // JSON-melding ved logging av datautlevering

	private String id;                           // Unik ID (brukes kun ved les/uthenting)
    private String person;                       // Fnr/dnr for personen dataene gjelder
    private String mottaker;                     // Orgnr som dataene leveres ut til
    private String tema;                         // Type data, som definert i https://modapp.adeo.no/kodeverksklient/viskodeverk, Tema
    private String behandlingsGrunnlag;			 // Beskriver hjemmel som er bakgrunn for at dataene utleveres TODO kodeverk e.l.
    private LocalDateTime uthentingsTidspunkt;   // Tidspunkt for utlevering, ISO-format uten tidssone
    private String leverteData;                  // Utleverte data, B64-encodet JSON
    private String samtykkeToken;                // JWT fra Altinn, encoded som i https://altinn.github.io/docs/guides/samtykke/datakilde/bruk-av-token/
    private String dataForespørsel;              // Brukes av noen (aareg) til å lagre requesten som resulterte i dataene som leveres
    private String leverandør;                   // Brukes av noen (aareg) til å lagre at mottaker har annet orgnr enn den avtalen er registrert på

    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPerson() {
		return person;
	}
	public void setPerson(String person) {
		this.person = person;
	}
	public String getMottaker() {
		return mottaker;
	}
	public void setMottaker(String mottaker) {
		this.mottaker = mottaker;
	}
	public String getTema() {
		return tema;
	}
	public void setTema(String tema) {
		this.tema = tema;
	}
	public String getBehandlingsGrunnlag() {
		return behandlingsGrunnlag;
	}
	public void setBehandlingsGrunnlag(String behandlingsGrunnlag) {
		this.behandlingsGrunnlag = behandlingsGrunnlag;
	}
	public LocalDateTime getUthentingsTidspunkt() {
		return uthentingsTidspunkt;
	}
	public void setUthentingsTidspunkt(LocalDateTime uthentingsTidspunkt) {
		this.uthentingsTidspunkt = uthentingsTidspunkt;
	}
	public String getLeverteData() {
		return leverteData;
	}
	public void setLeverteData(String leverteData) {
		this.leverteData = leverteData;
	}	
	public String getSamtykkeToken() {
		return samtykkeToken;
	}
	public void setSamtykkeToken(String samtykkeToken) {
		this.samtykkeToken = samtykkeToken;
	}
	public String getDataForespørsel() {
		return dataForespørsel;
	}
	public void setDataForespørsel(String dataForespørsel) {
		this.dataForespørsel = dataForespørsel;
	}
	public String getLeverandør() {
		return leverandør;
	}
	public void setLeverandør(String leverandør) {
		this.leverandør = leverandør;
	}
	@Override
	public String toString() {
		return "LoggMelding [id=" + id + ", person=" + person + ", mottaker=" + mottaker + ", tema=" + tema
				+ ", behandlingsGrunnlag=" + behandlingsGrunnlag + ", uthentingsTidspunkt=" + uthentingsTidspunkt
				+ ", leverteData=" + leverteData + ", samtykkeToken=" + samtykkeToken + ", dataForespørsel=" + dataForespørsel
				+ ", leverandør=" + leverandør + "]";
	}
	public static void main(String[] args) {
		String json = "{\"person\":\"srvABACPEP\", \"mottaker\":\"123456789\", \"tema\":\"ABC\", \"behandlingsGrunnlag\":\"hjemmel1\", \"uthentingsTidspunkt\":\"2020-11-25T12:24:21.675\", \"leverteData\":\"RGV0dGUgZXIgbWVsZGluZ2Vu\", \"dataForespørsel\":\"test med request i\", \"leverandør\":\"987654321\"}";
	    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {		
		    @Override
			public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		        return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_DATE_TIME);
		    }
		}).create();
		LoggMelding loggMelding = gson.fromJson(json, LoggMelding.class);
		System.out.println(loggMelding);
	}
}
