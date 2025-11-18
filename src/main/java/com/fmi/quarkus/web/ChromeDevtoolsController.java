package com.fmi.quarkus.web;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@PermitAll
@ApplicationScoped
public class ChromeDevtoolsController {

    @GET
    @Path(".well-known/appspecific/com.chrome.devtools.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response silenceChrome() {
        return Response.ok("{}").build();
    }

    @GET
    @Path("favicon.ico")
    @Produces(MediaType.APPLICATION_JSON)
    public String silenceChrome2() {
        return "{}";
    }
}


