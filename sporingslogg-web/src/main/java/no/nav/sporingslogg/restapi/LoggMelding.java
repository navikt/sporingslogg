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
    private String dataForespoersel;             // Brukes av noen (aareg) til å lagre requesten som resulterte i dataene som leveres
    private String leverandoer;                  // Brukes av noen (aareg) til å lagre at mottaker har annet orgnr enn den avtalen er registrert på

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
	public String getDataForespoersel() {
		return dataForespoersel;
	}
	public void setDataForespoersel(String dataForespoersel) {
		this.dataForespoersel = dataForespoersel;
	}
	public String getLeverandoer() {
		return leverandoer;
	}
	public void setLeverandoer(String leverandoer) {
		this.leverandoer = leverandoer;
	}
	@Override
	public String toString() {
		return "LoggMelding [id=" + id + ", person=" + person + ", mottaker=" + mottaker + ", tema=" + tema
				+ ", behandlingsGrunnlag=" + behandlingsGrunnlag + ", uthentingsTidspunkt=" + uthentingsTidspunkt
				+ ", leverteData=" + leverteData + ", samtykkeToken=" + samtykkeToken + ", dataForespoersel=" + dataForespoersel
				+ ", leverandoer=" + leverandoer + "]";
	}
}
