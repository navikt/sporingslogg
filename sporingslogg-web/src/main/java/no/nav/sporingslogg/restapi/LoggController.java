package no.nav.sporingslogg.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import no.nav.sporingslogg.ldap.LdapGroupService;
import no.nav.sporingslogg.tjeneste.LoggTjeneste;

@Path("/") // restcontext satt i web.xml
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoggController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoggTjeneste loggTjeneste;

    @Autowired
    private LdapGroupService ldapGroupService;

//    @Autowired
//    private RerunPENwithMessageLongerThan100K rerunPENwithMessageLongerThan100K;
    
    @Path("")
    @POST
    public Response logg(@Context SecurityContext securityContext, LoggMelding loggMelding) {
//        log.debug("Lagrer logg");

        String userName = getUserName(securityContext);  
        log.debug("Lagrer logg, user: " + userName);
        boolean brukerErIGruppe = ldapGroupService.brukerErIGruppe(userName);
        if (!brukerErIGruppe) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        Long loggId = loggTjeneste.lagreLoggInnslag(LoggConverter.fromJsonObject(loggMelding));
//        log.debug("Lagret logg med ny id " + loggId);
        LoggMeldingResponse response = new LoggMeldingResponse();
        response.setId(""+loggId);
        return Response.status(Response.Status.OK).entity(response).build();
    }
    
    // Midlertidig tjeneste for å rette opp i feil UFO-meldinger som havnet på topic 2/6-5/6 2020.
    // Kan gjenbrukes til andre feiltilfeller ved å reimplementere rerunUFOwithOrgnrPrefix og oppdatere app-context, 
    // har bare effekt så lenge feilede meldinger finnes på topic (normalt 1 uke etter innsending).
    // Gjenbrukt for PEN-meldinger 1/8-3/8 2020 med lengde over 100.000 bytes.
    @Path("manualrerun")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response manualRerun(@Context SecurityContext securityContext, @QueryParam("dryrun") String dryRunParam) {
    	
        boolean dryrun = true; // skal eksplisitt settes false for å kjøre "real"
        if (dryRunParam != null) {
        	try {
        		dryrun = Boolean.valueOf(dryRunParam);
        	} catch (Exception ignore) {
        	}
        }
        
        log.debug("ManualRerun kalt, med dryrun: " + dryrun);        
//        String response = rerunPENwithMessageLongerThan100K.performPollAndProcessing(dryrun);
        String response = "Not set up with any rerun functionality";
        return Response.status(Response.Status.OK).entity(response).build();
    }
    
	private String getUserName(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return null;
        }
        return securityContext.getUserPrincipal().getName();
    }
}

