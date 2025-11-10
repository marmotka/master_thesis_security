package com.fmi.quarkus.controller;

import com.fmi.quarkus.model.User;
import com.fmi.quarkus.service.UserService;
import com.fmi.quarkus.web.Templates;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

import java.security.Principal;
import java.util.List;

@Path("/admin")
@Blocking
@RolesAllowed("ADMIN")
public class AdminController {

    @Inject
    UserService users;

    @GET
    @Path("users")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance users(Principal p) {
        List<User> all = users.findAll();
        return Templates.Pages.admin_users(all, p.getName(), null, null);
    }

    @POST
    @Path("users/{id}/delete")
    public Object delete(@PathParam("id") Long id, Principal p) {
        var me = users.findByUsername(p.getName()).orElseThrow();
        var target = users.findById(id).orElse(null);
        if (target == null || target.id.equals(me.id)) {
            return Response.seeOther(java.net.URI.create("/admin/users")).build();
        }
        users.deleteById(id);
        return Response.seeOther(java.net.URI.create("/admin/users")).build();
    }
}
