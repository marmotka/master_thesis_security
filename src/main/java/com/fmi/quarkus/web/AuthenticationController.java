package com.fmi.quarkus.controller;

import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Blocking
@Produces(MediaType.TEXT_HTML)
@PermitAll
public class AuthenticationController {

    @Inject
    UserService userService;

    // Qute-checked templates (compile-time safe)
    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance login(String message, String error);
        public static native TemplateInstance register(RegisterForm form, String error);
    }

    // ----- LOGIN -----

    @GET
    @Path("login")
    public TemplateInstance login(@QueryParam("message") String message,
                                  @QueryParam("error") String error) {
        // Render login page. The actual POST goes to /j_security_check (see template below).
        return Tpl.login(message, error);
    }

    // ----- REGISTER -----

    @GET
    @Path("register")
    public TemplateInstance showRegister() {
        return Tpl.register(new RegisterForm(), null);
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Object doRegister(@BeanParam RegisterForm form) {
        // Server-side validation
        if (form.username == null || form.username.length() < 3
                || form.password == null || form.password.length() < 8
                || form.email == null || form.email.isBlank()) {
            return Tpl.register(form, "Please correct the highlighted errors.");
        }
        if (!form.password.equals(form.confirmPassword)) {
            return Tpl.register(form, "Passwords do not match.");
        }

        try {
            userService.register(form.username, form.email, form.password);
            // Redirect to login with a success message (banner)
            return Response.seeOther(java.net.URI.create("/login?message=Registration%20successful!")).build();
        } catch (IllegalArgumentException ex) {
            // e.g., duplicate email/username
            return Tpl.register(form, ex.getMessage());
        }
    }

    // ----- Simple form DTO bound from x-www-form-urlencoded -----
    public static class RegisterForm {
        @FormParam("username") public String username;
        @FormParam("email") public String email;
        @FormParam("password") public String password;
        @FormParam("confirmPassword") public String confirmPassword;
    }
}
