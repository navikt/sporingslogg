package no.nav.sporingslogg.web.fitnesse;

public class SleepFixture {
	
	public boolean sleepSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// ignore it
		}
		return true;
	}
}
