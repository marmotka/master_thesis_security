package com.fmi.quarkus.web;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/.well-known/appspecific")
@PermitAll
@ApplicationScoped
public class ChromeDevtoolsController {

    @GET
    @Path("com.chrome.devtools.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String silenceChrome() {
        return "{}";
    }
}


