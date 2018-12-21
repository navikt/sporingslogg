package no.nav.sporingslogg.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @Path("")
    @POST
    public Response logg(@Context SecurityContext securityContext, LoggMelding loggMelding) {
        log.debug("Lagrer logg");

        String userName = getUserName(securityContext);  
        log.debug("User: " + userName);
        boolean brukerErIGruppe = ldapGroupService.brukerErIGruppe(userName);
        if (!brukerErIGruppe) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        Long loggId = loggTjeneste.lagreLoggInnslag(LoggConverter.fromJsonObject(loggMelding));
        log.info("Lagret logg med ny id " + loggId);
        LoggMeldingResponse response = new LoggMeldingResponse();
        response.setId(""+loggId);
        return Response.status(Response.Status.OK).entity(response).build();
    }
    
	private String getUserName(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return null;
        }
        return securityContext.getUserPrincipal().getName();
    }
}

