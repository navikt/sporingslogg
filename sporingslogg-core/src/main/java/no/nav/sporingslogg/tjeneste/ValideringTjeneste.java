package no.nav.sporingslogg.tjeneste;

public class ValideringTjeneste {

    public int antallAarLagres(Integer antallAarLagres) {
        if (antallAarLagres == null || antallAarLagres < 1 || antallAarLagres > 10) {
            return 10;
        }
        return antallAarLagres;
    }

    public void validerIkkeBlank(String s, String label) {
        if (s == null || s.trim().length() == 0) {
            throw new IllegalArgumentException("Kan ikke ta imot tom verdi for " + label);
        }
    }

    public void validerIkkeTom(byte[] b, String label) {
        if (b == null || b.length == 0) {
            throw new IllegalArgumentException("Kan ikke ta imot tom " + label);
        }
    }

    public void validerMaxLengde(String s, int max, String label) {
        if (s != null && s.length() > max) {
        	String logString = s;
        	if (logString.length() > 100) {
        		logString = logString.substring(0,100) + "....";
        	}
            throw new IllegalArgumentException("Kan ikke ta imot verdi for "+label+" lenger enn " + max + " tegn: " + logString);
        }
    }

	public void validerMaxLengde(byte[] b, int max, String label) {
        if (b != null && b.length > max) {
            throw new IllegalArgumentException("Kan ikke ta imot "+label+" med lengde mer enn " + max + " bytes");
        }
	}
}
