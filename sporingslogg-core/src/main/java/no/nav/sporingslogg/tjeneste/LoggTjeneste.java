package no.nav.sporingslogg.tjeneste;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    
    public Long lagreLoggInnslag(LoggInnslag loggInnslag) {
        log.info("Lagrer for person " + loggInnslag.getPerson() + ", mottaker: " + loggInnslag.getMottaker() + ", tema: " + loggInnslag.getTema()); // TODO sett lengder ihht. DB-kolonner
        
        valideringTjeneste.validerIkkeBlank(loggInnslag.getPerson(), "person");
        valideringTjeneste.validerMaxLengde(loggInnslag.getPerson(), 11, "person");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getMottaker(), "mottaker");
        valideringTjeneste.validerMaxLengde(loggInnslag.getMottaker(), 9, "mottaker");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getTema(), "tema");
        valideringTjeneste.validerMaxLengde(loggInnslag.getTema(), 3, "tema");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getHjemmel(), "hjemmel");
        valideringTjeneste.validerMaxLengde(loggInnslag.getHjemmel(), 100, "hjemmel");
        valideringTjeneste.validerIkkeBlank(loggInnslag.getLeverteData(), "data");
        valideringTjeneste.validerMaxLengde(loggInnslag.getLeverteData(), 4000, "data"); 
        valideringTjeneste.validerMaxLengde(loggInnslag.getSamtykkeToken(), 1000, "data"); 
        
        if (loggInnslag.getUthentingsTidspunkt() == null) {
        	loggInnslag.setUthentingsTidspunkt(timestampTjeneste.now());
        }
        entityManager.persist(loggInnslag);
        entityManager.flush();
        
        Long id = loggInnslag.getId();
        log.debug("Melding lagret med unik ID " + id);
        return id; 
    }

    public List<LoggInnslag> finnAlleLoggInnslagForPerson(String person) {
        return entityManager.createQuery("from LoggInnslag where person = :p", LoggInnslag.class)
        		.setParameter("p", person)
        		.getResultList();
    }

    public boolean isReady() {
		// Brukes til ping/selftest
		return entityManager != null && valideringTjeneste != null && timestampTjeneste != null;
	}    
}
