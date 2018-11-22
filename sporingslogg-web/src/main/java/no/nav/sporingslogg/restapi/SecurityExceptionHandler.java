package no.nav.sporingslogg.restapi;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class SecurityExceptionHandler implements ExceptionMapper<SecurityException> {

    /*
     * CXF gir sær XML tilbake ved SecurityExceptions, som ikke gjør seg så bra når klient forventer JSON.
     * Velger å returnere exception-meldingen som en ren tekst, ikke pakket inn som JSON eller annet.
     */
    @Override
    public Response toResponse(SecurityException exception) {
        return Response.status(Status.FORBIDDEN)
                .encoding("text/plain")
                .entity(exception.getMessage())
                .build();
    }
}
