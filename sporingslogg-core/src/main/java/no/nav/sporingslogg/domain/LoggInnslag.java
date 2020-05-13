package no.nav.sporingslogg.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="SPORINGS_LOGG") 
public class LoggInnslag {                // Logger en uthenting av data om en person
    
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @SequenceGenerator(name = "sporingsLoggIdGenerator", sequenceName = "SPORINGS_LOGG_ID_SEQ", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sporingsLoggIdGenerator")
    private Long id;
    
    @Column(name = "PERSON", nullable = false)
    private String person; 
    
    @Column(name = "MOTTAKER", nullable = false)
    private String mottaker;
    
    @Column(name = "TEMA", nullable = false)
    private String tema;     
    
    @Column(name = "HJEMMEL", nullable = false)
    private String hjemmel;						 

    @Column(name = "TIDSPUNKT", nullable = false)
    @Convert(converter = TimestampConverter.class)
    private LocalDateTime uthentingsTidspunkt;

    @Lob
    @Column(name = "LEVERTE_DATA", nullable = false)
    private String leverteData;  // dataene som ble hentet ut, B64-encodet JSON-format
    
    @Column(name = "SAMTYKKE_TOKEN", nullable = true)
    private String samtykkeToken;  // JWT hvis hjemmel er 'samtykke'
    
    LoggInnslag() {
        // brukes av JPA
    }

	public LoggInnslag(String person, String mottaker, String tema, String hjemmel, LocalDateTime uthentingsTidspunkt, String leverteData, String samtykkeToken) {
		this.person = person;
		this.mottaker = mottaker;
		this.tema = tema;
		this.hjemmel = hjemmel;
		this.uthentingsTidspunkt = uthentingsTidspunkt;
		this.leverteData = leverteData;
		this.samtykkeToken = samtykkeToken;
	}

	public Long getId() {
		return id;
	}

	public String getPerson() {
		return person;
	}

	public String getMottaker() {
		return mottaker;
	}

	public String getTema() {
		return tema;
	}

	public String getHjemmel() {
		return hjemmel;
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

	public String getSamtykkeToken() {
		return samtykkeToken;
	}

	@Override
	public String toString() {
		return "LoggInnslag [id=" + id + ", person=" + person + ", mottaker=" + mottaker + ", tema=" + tema
				+ ", hjemmel=" + hjemmel + ", uthentingsTidspunkt=" + uthentingsTidspunkt + ", leverteData="
				+ leverteData + ", samtykkeToken=" + samtykkeToken + "]";
	}
}
