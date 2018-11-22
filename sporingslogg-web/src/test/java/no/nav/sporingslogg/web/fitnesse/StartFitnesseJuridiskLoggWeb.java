package no.nav.sporingslogg.web.fitnesse;

import fitnesseMain.Arguments;
import fitnesseMain.FitNesseMain;

public class StartFitnesseJuridiskLoggWeb {
	
	private static final int FITNESSE_PORT = 9090;
	static final String FITNESSE_HOME = "src/test/resources/fitnesse_tester_eksternserver";

	private static final String fitnesseCommandLine = "-v -p "+FITNESSE_PORT+" -d "+FITNESSE_HOME+" -e 0";

	public static void main(String[] args) {
				
		System.out.println("Starting StartFitnesseJuridiskLoggWeb at port " + FITNESSE_PORT);
		try {
			new FitNesseMain().launchFitNesse(new Arguments(fitnesseCommandLine.split(" "))); // kan bruke ContextConfigurator
			System.out.println("StartFitnesseJuridiskLoggWeb started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
