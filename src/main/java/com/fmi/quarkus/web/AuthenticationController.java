package com.fmi.quarkus.web;

import com.fmi.quarkus.exception.DuplicateEmailException;
import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Path("/")
@Blocking
@Produces(MediaType.TEXT_HTML)
@PermitAll
public class AuthenticationController {

    @Inject
    SecurityIdentity identity;

    @Inject
    UserService userService;

    // Qute-checked templates (compile-time safe)
    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance login(String message, String error, SecurityIdentity identity);

        public static native TemplateInstance register(RegisterRequest request, Map<String, String> errors, String error, SecurityIdentity identity);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Authenticated  // Require login
    public Response home() {
        return Response.seeOther(UriBuilder.fromPath("/tasks/view").build())
                .build();
    }

    // ----- LOGIN -----

    @GET
    @Path("login")
    public TemplateInstance login(@QueryParam("message") @DefaultValue("") String message,
                                  @QueryParam("error") @DefaultValue("") String error,
                                  @QueryParam("logout") @DefaultValue("") String logout) {

        if (!logout.isEmpty()) {
            message = "You have been logged out successfully.";
        }
        if (error != null && error.matches("true")) {
            error = "Invalid username or password";
        }

        return Tpl.login(message, error, identity)
                .data("errorMessage", error);
    }


    // ----- REGISTER -----

    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showRegister() {
        return Tpl.register(new RegisterRequest(), Map.of(), null, identity);
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance register(@BeanParam RegisterRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.password == null || request.password.length() < 8)
            errors.put("password", "Password must be at least 8 characters long");

        if (!Objects.equals(request.password, request.confirmPassword))
            errors.put("confirmPassword", "Passwords do not match");

        if (!errors.isEmpty())
            return Tpl.register(request, errors, null, identity);

        try {
            userService.register(request);
        } catch (DuplicateEmailException ex) {
            errors.put("email", ex.getMessage());
            return Tpl.register(request, errors, null, identity);
        }
        return Tpl.login("Registration successful. Please sign in.", null, identity);
    }

    // ----- LOGOUT -----

    @POST
    @Path("/logout")
    @PermitAll
    public Response logout(@Context RoutingContext rc) {
        // Destroy Vert.x web session if present
        try {
            if (rc.session() != null) rc.session().destroy();
        } catch (Throwable ignored) {
        }

        // Expire auth cookies set by Quarkus form auth
        String expireCred = "quarkus-credential=; Max-Age=0; Path=/; HttpOnly; SameSite=Strict";
        String expireRedir = "quarkus-redirect-location=; Max-Age=0; Path=/; SameSite=Strict";

        return Response.seeOther(URI.create("/login?logout=true"))
                .header("Set-Cookie", expireCred)
                .header("Set-Cookie", expireRedir)
                .build();
    }


    // ----- Simple form DTO bound from x-www-form-urlencoded -----
    public static class RegisterRequest {
        @FormParam("username")
        public String username;
        @FormParam("email")
        public String email;
        @FormParam("password")
        public String password;
        @FormParam("confirmPassword")
        public String confirmPassword;
    }
}
