package com.fmi.quarkus.controller;

import com.fmi.quarkus.service.UserService;
import com.fmi.quarkus.web.Templates;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Path("/profile")
@Blocking
@RolesAllowed({"USER", "ADMIN"})
public class ProfileController {

    @Inject
    UserService users;

    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance profile(String currentUsername, String currentEmail, java.util.Map<String,Object> passwordForm, Boolean openPassword);
    }

//    @GET
//    @Path("view")
//    @Produces(MediaType.TEXT_HTML)
//    public TemplateInstance view(Principal p, @QueryParam("openPassword") Boolean open) {
//        var u = users.findByUsername(p.getName()).orElseThrow();
//        Map<String, Object> passwordForm = new HashMap<>();
//        passwordForm.put("currentPassword", "");
//        passwordForm.put("newPassword", "");
//        passwordForm.put("confirmPassword", "");
//        return Templates.Pages.profile(u.username, u.email, passwordForm, open != null ? open : false);
//    }

    @GET @Path("view")
    public TemplateInstance view(@Context SecurityContext ctx, @QueryParam("openPassword") @DefaultValue("false") boolean open) {
        var u = users.findByUsername(ctx.getUserPrincipal().getName()).orElseThrow();
        var form = new java.util.HashMap<String,Object>();
        form.put("currentPassword", "");
        form.put("newPassword", "");
        form.put("confirmPassword", "");
        return Tpl.profile(u.username, u.email, form, open);
    }

    @POST
    @Path("password")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object change(@FormParam("currentPassword") String current,
                         @FormParam("newPassword") String n1,
                         @FormParam("confirmPassword") String n2,
                         Principal p) {
        if (n1 == null || n1.length() < 8 || !n1.equals(n2)) {
            return Response.seeOther(java.net.URI.create("/profile/view?openPassword=true")).build();
        }
        users.changePassword(p.getName(), current, n1);
        return Response.seeOther(java.net.URI.create("/profile/view")).build();
    }
}
