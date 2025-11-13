package com.fmi.quarkus.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/users")
public class UserController {

    @GET
    @RolesAllowed("user")
    @Path("/current-user")
    public String getCurrentUser(@Context SecurityContext securityContext) {
        return securityContext.getUserPrincipal().getName();
    }
}