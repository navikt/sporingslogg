package no.nav.sporingslogg.restapi;

//import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import no.nav.sporingslogg.tjeneste.LoggTjeneste;

@Path("/") // restcontext satt i web.xml
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PingController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoggTjeneste loggTjeneste;

	@Path("")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
//        log.debug("Ping er kalt");
        if (loggTjeneste.isReady()) {
            log.debug("Ping returnerer OK"); 
            return Response.status(Response.Status.OK).entity("OK").build();
        }
        log.warn("Ping returnerer feil");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Tjenesten er ikke klar").build();
    }
}

