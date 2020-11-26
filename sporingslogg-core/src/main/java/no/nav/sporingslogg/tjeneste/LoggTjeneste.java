package no.nav.sporingslogg.tjeneste;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.Counter;
import no.nav.sporingslogg.domain.LoggInnslag;

@Component
@Transactional
public class LoggTjeneste {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private TimestampTjeneste timestampTjeneste;
    @Autowired
    private ValideringTjeneste valideringTjeneste;
    
    private static final Counter lagredeLoggInnslag = Counter.build().name("sporingslogg_logger").help("Lagrede logginnslag").register();
    private static final Counter loggOppslag = Counter.build().name("sporingslogg_oppslag").help("Loggoppslag").register();

    public Long lagreLoggInnslag(LoggInnslag loggInnslag) {
        log.info("Lagrer for person " + scrambleFnr(loggInnslag.getPerson()) + ", mottaker: " + loggInnslag.getMottaker() + ", tema: " + loggInnslag.getTema()); 
        
        valideringTjeneste.validerIkkeBlank(loggInnslag.getPerson(), "person");
        valideringTjeneste.validerMaxLengde(loggInnslag.getPerson(), 11, "person");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getMottaker(), "mottaker");
        valideringTjeneste.validerMaxLengde(loggInnslag.getMottaker(), 9, "mottaker");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getTema(), "tema");
        valideringTjeneste.validerMaxLengde(loggInnslag.getTema(), 3, "tema");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getHjemmel(), "hjemmel");
        valideringTjeneste.validerMaxLengde(loggInnslag.getHjemmel(), 100, "hjemmel");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getLeverteData(), "data");
        valideringTjeneste.validerMaxLengde(loggInnslag.getLeverteData(), 1000000, "data"); 
        valideringTjeneste.validerMaxLengde(loggInnslag.getSamtykkeToken(), 1000, "samtykketoken"); 
        valideringTjeneste.validerMaxLengde(loggInnslag.getForesporsel(), 100000, "forespørsel"); 
        valideringTjeneste.validerMaxLengde(loggInnslag.getLeverandor(), 9, "leverandør"); 
        
        if (loggInnslag.getUthentingsTidspunkt() == null) {
        	loggInnslag.setUthentingsTidspunkt(timestampTjeneste.now());
        }
        entityManager.persist(loggInnslag);
        entityManager.flush();
        lagredeLoggInnslag.inc();
        
        Long id = loggInnslag.getId();
        log.debug("Melding lagret med unik ID " + id);
        return id; 
    }

    public List<LoggInnslag> finnAlleLoggInnslagForPerson(String person) {
    	loggOppslag.inc();
        return entityManager.createQuery("from LoggInnslag where person = :p", LoggInnslag.class)
        		.setParameter("p", person)
        		.getResultList();
    }

    public boolean isReady() {
		// Brukes til ping/selftest
		//return entityManager != null && valideringTjeneste != null && timestampTjeneste != null;
    	// er ent av og til null?
		return true;
	}  
    
    public static String scrambleFnr(String fnr) { // Bytt ut de 5 siste tegnene av et reelt fnr med xxxxx
    	if (fnr == null || fnr.length() < 6) {
    		return fnr;
    	}
    	return fnr.substring(0,6)+"xxxxx";
    }
}
