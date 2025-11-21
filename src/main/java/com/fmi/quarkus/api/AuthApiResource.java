package com.fmi.quarkus.api;

import com.fmi.quarkus.api.dto.ApiLoginRequest;
import com.fmi.quarkus.api.dto.TokenResponse;
import com.fmi.quarkus.model.Role;
import com.fmi.quarkus.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Path("/api/auth")
@Blocking
@Produces(MediaType.APPLICATION_JSON)
public class AuthApiResource {

    private static final Logger log = Logger.getLogger(AuthApiResource.class);

    private static final long TOKEN_LIFETIME_MINUTES = 15;
    public static final String TASKMANAGER_QUARKUS = "taskmanager-quarkus";

    @POST
    @Path("/login")
    @PermitAll
    @Transactional
    @Consumes({"application/json", "application/json;charset=UTF-8", "application/json; charset=UTF-8"})
    //needed for local testing/bug in insomnia
    public Response login(ApiLoginRequest request) {

        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return unauthorized("Invalid credentials");
        }

        User user = User.findByUsername(request.getUsername());
        if (user == null) {
            log.warnf("API login failed: unknown user '%s'", request.getUsername());
            return unauthorized("Invalid username() or password");
        }

        if (!BcryptUtil.matches(request.getPassword(), user.password)) {
            log.warnf("API login failed: bad password for '%s'", request.getUsername());
            return unauthorized("Invalid username or password");
        }

        Role role = user.role != null ? user.role : Role.USER;
        Set<String> groups = Set.of(role.name());

        Instant now = Instant.now();
        Instant exp = now.plus(TOKEN_LIFETIME_MINUTES, ChronoUnit.MINUTES);

        String token = Jwt.claims()
                .issuer(TASKMANAGER_QUARKUS)
                .subject(user.username)
                .upn(user.username)
                .groups(groups)
                .issuedAt(now)
                .expiresAt(exp)
                .sign();

        TokenResponse response = new TokenResponse(
                token,
                exp.getEpochSecond(),
                user.username,
                groups
        );

        return Response.ok(response).build();
    }

    private Response unauthorized(String message) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(java.util.Map.of(
                        "success", false,
                        "message", message
                ))
                .build();
    }
}
