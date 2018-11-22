package no.nav.sporingslogg.web.fitnesse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;
import no.nav.sporingslogg.kafkatestclient.EmbeddedKafkaMain;
import no.nav.sporingslogg.standalone.testconfig.EmulateLoginInterceptor;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain.DbConfig;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain.KafkaConfig;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain.LoginConfig;
import no.nav.sporingslogg.standalone.testconfig.StandaloneTestJettyMain.OidcConfig;
import no.nav.sporingslogg.standalone.testconfig.UrlUsernamePassword;

public class StartFitnesseWithJettySporingsLoggWeb {
	
	private static final int FITNESSE_PORT = 9190; // Brukes når denne startes med main() i denne klassen (ikke som Junit test)
	static final int JETTY_PORT = 9199; // Brukes når denne startes med main() i denne klassen (ikke som Junit test)
	static final String FITNESSE_HOME = "src/test/resources/fitnesse_tester_lokaljetty";

	static final UrlUsernamePassword DB_CONNECTION = StandaloneTestJettyMain.HSQL_SERVER; // Brukes av fixtures som går rett mot DB
	
	private static final String fitnesseCommandLine = "-v -p "+FITNESSE_PORT+" -d "+FITNESSE_HOME+" -e 0";

	public static void main(String[] args) {
				
		System.out.println("Starting StartFitnesseWithJettySporingsLoggWeb at port " + FITNESSE_PORT);
		try {
			
			startJettyOgKafka(JETTY_PORT);
			
			new FitNesseMain().launchFitNesse(new Arguments(fitnesseCommandLine.split(" ")));
			
			System.out.println("StartFitnesseWithJettySporingsLoggWeb started at port " + FITNESSE_PORT);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void startJettyOgKafka(int port) throws Exception {
		EmulateLoginInterceptor.USER_TO_REFUSE = "IKKELOV"; // For at test med ikke-autorisert bruker skal funke

		startLocalJetty(port); 
  	
		// Start embedded Kafka hvis den skal brukes (ellers vil ikke tester med kafka fungere)
		startEmbeddedKafka();
	}

	public static void startEmbeddedKafka() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {	
			@Override
			public void run() {
				EmbeddedKafkaMain.setupAndStart(StandaloneTestJettyMain.SPORINGS_LOGG_TOPIC);
			}
		});
	}
	public static void startLocalJetty(int port) throws Exception {
		LocalJettyRestFixture.JETTY_PORT = port;
		System.out.println("Starting Local Jetty for StartFitnesseWithJettySporingsLoggWeb at port " + port);
		new StandaloneTestJettyMain(OidcConfig.DUMMY, KafkaConfig.EMBEDDED, LoginConfig.DUMMY, DbConfig.HSQL_SERVER, port, false);
	}
}
