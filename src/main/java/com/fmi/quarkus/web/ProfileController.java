package com.fmi.quarkus.web;

import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.reactive.RestForm;   // <-- use RestForm for form fields

@Path("/profile")
@Blocking
@RolesAllowed({"USER", "ADMIN"})
public class ProfileController {

    @Inject SecurityIdentity identity;
    @Inject UserService users;

    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance profile(
                String currentUsername,
                String currentEmail,
                java.util.Map<String, Object> passwordForm,
                Boolean openPassword,
                SecurityIdentity identity
        );
    }

    @GET
    @Path("view")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance view(@Context SecurityContext ctx,
                                 @QueryParam("openPassword") @DefaultValue("false") boolean open) {
        var u = users.findByUsername(ctx.getUserPrincipal().getName()).orElseThrow();
        var form = new java.util.HashMap<String, Object>();
        form.put("currentPassword", "");
        form.put("newPassword", "");
        form.put("confirmPassword", "");
        return Tpl.profile(u.username, u.email, form, open, identity);
    }

    @POST
    @Path("password")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response change(@RestForm String currentPassword,
                           @RestForm String newPassword,
                           @RestForm String confirmPassword) {

        if (newPassword == null || newPassword.length() < 8 || !newPassword.equals(confirmPassword)) {
            return Response.seeOther(java.net.URI.create("/profile/view?openPassword=true")).build();
        }

        String username = identity.getPrincipal().getName();
        users.changePassword(username, currentPassword, newPassword);

        return Response.seeOther(java.net.URI.create("/profile/view")).build();
    }
}
