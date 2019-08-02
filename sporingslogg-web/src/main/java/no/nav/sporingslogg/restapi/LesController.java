package no.nav.sporingslogg.restapi;

import java.util.ArrayList;
import java.util.List;

//import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import no.nav.sporingslogg.domain.LoggInnslag;
import no.nav.sporingslogg.oidc.OidcAuthenticate;
import no.nav.sporingslogg.tjeneste.LoggTjeneste;

@Path("/") // restcontext satt i web.xml
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LesController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoggTjeneste loggTjeneste;

    @Autowired
    private OidcAuthenticate oidcAuthenticate;

    @Path("")
    @GET
    public Response les(@Context SecurityContext securityContext, @Context HttpHeaders headers) { // Beskyttet via access-token
        String innloggetPerson = validerAccessToken(headers);
    	log.info("Henter logger for person " + innloggetPerson);
        
        List<LoggMelding> response = new ArrayList<LoggMelding>();        
        List<LoggInnslag> liste = loggTjeneste.finnAlleLoggInnslagForPerson(innloggetPerson);
        log.debug("Fant " + liste.size() + " logger");
        for (LoggInnslag loggInnslag : liste) {
			response.add(LoggConverter.toJsonObject(loggInnslag));
		}
        return Response.status(Response.Status.OK).entity(response).build();
    }

	private String validerAccessToken(HttpHeaders headers) {
		if (headers == null || headers.getRequestHeader(HttpHeaders.AUTHORIZATION) == null || headers.getRequestHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
			log.debug("Ingen Authorization header i request");
            throw new SecurityException("Ingen Authorization header i request");
    	}
    	List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
    	String bearerHeader = startsWith("Bearer", authHeaders);
    	if (bearerHeader == null) {
			log.debug("Ingen Bearer Authorization header i request");
            throw new SecurityException("Ingen Bearer Authorization header i request");
    	}

		try {
			String token = bearerHeader.substring("Bearer".length()).trim();
			return oidcAuthenticate.getVerifiedSubject(token);  // bruk unVerified i test hvis man ikke kan validere token
			//return oidcAuthenticate.getUnverifiedSubject(token); 
		} catch (RuntimeException e) {
			log.debug("OIDC validering feiler: ", e);
            throw new SecurityException("OIDC-token validering feiler", e);  // Det er mulig å få feil her pga. trøbbel med å nå OIDC-issuer, som er et teknisk problem, men oftest feil i token
		}
	}
    
    private String startsWith(String s, List<String> authHeaders) {
		for (String header : authHeaders) {
			if (header.startsWith(s)) {
				return header;
			}
		}
		return null;
	}
}

