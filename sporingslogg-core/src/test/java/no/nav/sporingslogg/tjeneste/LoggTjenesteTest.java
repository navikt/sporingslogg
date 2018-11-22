package no.nav.sporingslogg.tjeneste;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.nav.sporingslogg.domain.LoggInnslag;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/test-hsqldb-context.xml"}) // bruk test-localoracle-context.xml for test mot lokal oracle. 
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // får ikke renset DB, må cleare i kode...
public class LoggTjenesteTest {

    private static final String NOW = "2017-04-24T13:13:13.123456";
    
    @Autowired
    LoggTjeneste loggTjeneste;
    
    @Autowired
    DbClearUtil dbClearUtil;
    
    @Before
    public void clearDb() {
    	dbClearUtil.clearDb();
    }
    
    @Test
    public void lagreOgGjenfinnLogg() {
        
        HardCodedTimestampTjeneste.NOW = NOW;
        LoggInnslag l = new LoggInnslag("12345678901", "123456789", "QQQ", "har lov", null, "tull1", null);
        Long id = loggTjeneste.lagreLoggInnslag(l);
        List<LoggInnslag> alleLoggInnslagForPerson = loggTjeneste.finnAlleLoggInnslagForPerson("12345678901");
        assertEquals("Gjenfunnet logginnslag", 1, alleLoggInnslagForPerson.size());
        LoggInnslag loggInnslag = alleLoggInnslagForPerson.get(0);
        sjekkLoggLagring(id, loggInnslag);

    }

    private void sjekkLoggLagring(Long id, LoggInnslag l) {        
        assertEquals("id", id, l.getId());
        assertEquals("person", "12345678901", l.getPerson());
        assertEquals("mottaker", "123456789", l.getMottaker());
        assertEquals("data", "tull1", l.getLeverteData());
        assertEquals("TS", HardCodedTimestampTjeneste.NOW, l.getUthentingsTidspunkt().toString());
    }
}
