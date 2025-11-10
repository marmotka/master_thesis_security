package com.fmi.quarkus.api;


import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
        import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Set;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthApi {

    // In real life, you'd authenticate here (e.g., using IdentityProvider)
    // For brevity, assume a separate authentication manager or a form-equivalent check;
    // mint token with username & roles:
    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(Credentials c) {
        // TODO: authenticate against DB (reuse UserService) and verify password
        // If ok:
        String token = Jwt.upn(c.username)
                .groups(Set.of("USER"))
                .sign();
        return Response.ok(java.util.Map.of("token", token)).build();
    }

    public static class Credentials { public String username; public String password; }
}
