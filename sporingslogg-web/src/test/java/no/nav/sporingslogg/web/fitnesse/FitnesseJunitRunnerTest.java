package no.nav.sporingslogg.web.fitnesse;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fitnesse.junit.JUnitHelper;

// Kafka-biten feiler herfra. Fiks det eller ta ut kafka-testen fra suiten som junit
@Ignore
public class FitnesseJunitRunnerTest {

	private static final int FITNESSE_PORT = 9290; // Brukes når denne startes som Junit test
	private static final int JETTY_PORT = 9299; // Brukes når denne startes som Junit test
    private JUnitHelper junitHelper;

    @Before
    public void prepare() {
    	try {
			StartFitnesseWithJettySporingsLoggWeb.startJettyOgKafka(JETTY_PORT);
			Thread.sleep(1000); // Tar tid å starte Kafka
		} catch (Exception e) {
			throw new RuntimeException("Oppstart av Jetty feiler", e);
		} 
        junitHelper = new JUnitHelper(StartFitnesseWithJettySporingsLoggWeb.FITNESSE_HOME, new File("target", "fitnesse-junit").getAbsolutePath());
        junitHelper.setPort(FITNESSE_PORT);
    }

    @Test
    public void runAllFitnesseTests() throws Exception {
        junitHelper.assertSuitePasses("SporingsLoggRestTjenester");
    }
}
