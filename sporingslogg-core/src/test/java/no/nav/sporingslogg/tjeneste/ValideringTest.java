package no.nav.sporingslogg.tjeneste;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.nav.sporingslogg.domain.LoggInnslag;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/test-hsqldb-context.xml"}) // DB brukes egentlig ikke, men contexten trengs 
public class ValideringTest {

    @Autowired
    LoggTjeneste loggTjeneste;
    @Autowired
    ValideringTjeneste valideringTjeneste;
    
    @Test
    public void nullPersonGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l(null, "org", "meldingen"));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    private LoggInnslag l(String p, String m, String d) {
		return new LoggInnslag(p, m, "xxx", "hhh", null, d, null, null, null);
	}
	@Test
    public void tomPersonGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("  ", "org", "meldingen"));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void forLangPersonGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("123456789012", "org", "meldingen"));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void nullOrgGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("id", null, "meldingen"));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void tomOrgGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("id", "  ", "meldingen"));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void forLangOrgGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("id", "1234567890", "meldingen"));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void nullDataGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("id", "org", null));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void tomDataGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("id", "org", ""));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void forLangDataGirValideringsFeil() {
        try {
            loggTjeneste.lagreLoggInnslag(l("id", "org", string(1000001)));
            fail("Forventet exception");
        } catch (Exception e) {
            // OK
        }
    }
    @Test
    public void langDataErOk() {
        loggTjeneste.lagreLoggInnslag(l("id", "org", string(1000000)));
    }
	private String string(int l) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < l; i++) {
			sb.append("q");
		}
		return sb.toString();
	}

}
