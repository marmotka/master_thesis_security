package com.fmi.quarkus.web;

import com.fmi.quarkus.dto.UserDto;
import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/admin/users")
@Blocking
@RolesAllowed("ADMIN")
@Produces(MediaType.TEXT_HTML)
public class AdminController {

    @Inject
    SecurityIdentity identity;

    @Inject
    UserService userService;

    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance users(List<UserDto> users,
                                                    SecurityIdentity identity,
                                                    String successMessage,
                                                    String errorMessage);
    }

    private String currentAdmin() {
        return identity.getPrincipal().getName();
    }

    @GET
    public TemplateInstance list(@QueryParam("notice") @DefaultValue("") String notice) {
        List<UserDto> users = userService.findAllUsersForAdmin();

        String success = "";
        String error   = "";

        switch (notice) {
            case "deleted"    -> success = "User deleted successfully.";
            case "selfDelete" -> error   = "You cannot delete your own account.";
            case "notfound"   -> error   = "User not found.";
            case "lastAdmin"  -> error   = "Cannot delete the last admin user.";
        }

        return Tpl.users(users, identity, success, error);
    }

    @POST
    @Path("{id}/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response delete(@PathParam("id") Long id) {
        String admin = currentAdmin();
        try {
            userService.deleteUserAsAdmin(id, admin);
            return Response.seeOther(URI.create("/admin/users?notice=deleted")).build();

        } catch (IllegalArgumentException ex) {
            // Self delete attempt
            return Response.seeOther(URI.create("/admin/users?notice=selfDelete")).build();

        } catch (IllegalStateException ex) {
            // Last admin case
            return Response.seeOther(URI.create("/admin/users?notice=lastAdmin")).build();

        } catch (EntityNotFoundException ex) {
            return Response.seeOther(URI.create("/admin/users?notice=notfound")).build();
        }
    }
}
