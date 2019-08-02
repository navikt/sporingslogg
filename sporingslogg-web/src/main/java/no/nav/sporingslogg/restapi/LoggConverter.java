package no.nav.sporingslogg.restapi;

import no.nav.sporingslogg.domain.LoggInnslag;

public class LoggConverter {

    public static LoggMelding toJsonObject(LoggInnslag loggInnslag) {
		LoggMelding loggMelding = new LoggMelding();
		loggMelding.setId(""+loggInnslag.getId());
		loggMelding.setPerson(loggInnslag.getPerson());
		loggMelding.setMottaker(loggInnslag.getMottaker());
		loggMelding.setTema(loggInnslag.getTema());
		loggMelding.setBehandlingsGrunnlag(loggInnslag.getHjemmel());
		loggMelding.setUthentingsTidspunkt(loggInnslag.getUthentingsTidspunkt());
		loggMelding.setLeverteData(loggInnslag.getLeverteData());
		loggMelding.setSamtykkeToken(loggInnslag.getSamtykkeToken());
		return loggMelding;
	}

    public static LoggInnslag fromJsonObject(LoggMelding loggMelding) {
    	return new LoggInnslag(loggMelding.getPerson(), loggMelding.getMottaker(), loggMelding.getTema(),
    			loggMelding.getBehandlingsGrunnlag(), loggMelding.getUthentingsTidspunkt(), loggMelding.getLeverteData(), loggMelding.getSamtykkeToken());
	}

}
