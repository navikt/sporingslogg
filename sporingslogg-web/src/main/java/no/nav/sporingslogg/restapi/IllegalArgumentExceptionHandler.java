package no.nav.sporingslogg.restapi;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class IllegalArgumentExceptionHandler implements ExceptionMapper<IllegalArgumentException> {

    /*
     * Feil i input
     */
    @Override
    public Response toResponse(IllegalArgumentException exception) {
        return Response.status(Status.BAD_REQUEST)
                .encoding("text/plain")
                .entity(exception.getMessage())
                .build();
    }
}
