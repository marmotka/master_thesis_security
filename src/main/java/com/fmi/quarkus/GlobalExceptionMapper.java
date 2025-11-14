package com.fmi.quarkus;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class);


    @Inject
    Template error_page;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable ex) {
        log.error(ex.getMessage(), ex);
        // If the app deliberately set a status (e.g., NotFoundException)
        int status = (ex instanceof WebApplicationException wae)
                ? wae.getResponse().getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        // Content negotiation: HTML for browsers, JSON for APIs
        boolean wantsHtml = headers.getAcceptableMediaTypes()
                .stream()
                .anyMatch(mt -> mt.isCompatible(MediaType.TEXT_HTML_TYPE));

        if (wantsHtml) {
            TemplateInstance page = error_page
                    .data("title", status >= 500 ? "Something went wrong" : "Request error")
                    .data("status", status)
                    .data("message", status >= 500 ? "An unexpected error occurred." : ex.getMessage());
            return Response.status(status).entity(page).type(MediaType.TEXT_HTML).build();
        }

        // JSON fallback (no stack trace)
        var body = Map.of(
                "status", status,
                "error", status >= 500 ? "Internal Server Error" : ex.getClass().getSimpleName(),
                "message", status >= 500 ? "An unexpected error occurred." : ex.getMessage()
        );
        return Response.status(status).entity(body).type(MediaType.APPLICATION_JSON).build();
    }
}
