package com.fmi.quarkus.web;

import com.fmi.quarkus.model.User;
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

import java.security.Principal;
import java.util.List;

@Path("/admin")
@Blocking
@RolesAllowed("ADMIN")
public class AdminController {

    @Inject
    SecurityIdentity identity;

    @Inject
    UserService userService;

    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance users(List<User> users, String currentUsername, String successMessage, String errorMessage, SecurityIdentity identity);
    }

    @GET
    @Path("users")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("admin")
    public TemplateInstance users() {  // No params!
        List<User> all = userService.findAll();
        String username = identity.getPrincipal().getName();  // Direct from injected identity

        return Tpl.users(all, username, null, null, identity);
    }

    @POST
    @Path("/users/{id}/delete")
    public Response delete(@PathParam("id") Long id, Principal principal) {
        var me = userService.findByUsername(principal.getName()).orElseThrow();
        var target = userService.findById(id).orElse(null);
        if (target == null) {
            return Response.seeOther(java.net.URI.create("/admin/users?errorMessage=User%20not%20found")).build();
        }
        if (target.id.equals(me.id)) {
            return Response.seeOther(java.net.URI.create("/admin/users?errorMessage=You%20cannot%20delete%20your%20own%20account")).build();
        }
        userService.deleteById(id);
        return Response.seeOther(java.net.URI.create("/admin/users?successMessage=User%20deleted")).build();
    }
}
