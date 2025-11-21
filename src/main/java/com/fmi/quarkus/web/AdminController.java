package com.fmi.quarkus.web;

import com.fmi.quarkus.dto.UserDto;
import com.fmi.quarkus.service.UserService;
import com.fmi.quarkus.util.CommonNames;
import com.fmi.quarkus.util.Notice;
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

import java.util.List;

@Path("/admin/users")
@Blocking
@RolesAllowed(CommonNames.ADMIN)
@Produces(MediaType.TEXT_HTML)
public class AdminController {

    public static final String ADMIN_USERS_PATH = "/admin/users";
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
        String error = "";

        if (!notice.isBlank()) {
            try {
                Notice n = Notice.valueOf(notice.toUpperCase());
                if ("success".equals(n.type())) {
                    success = n.message();
                } else {
                    error = n.message();
                }
            } catch (IllegalArgumentException ex) {
                // unknown notice → ignore silently
            }
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
            return Response.seeOther(Notice.DELETED.redirectTo(ADMIN_USERS_PATH)).build();

        } catch (IllegalArgumentException ex) {
            return Response.seeOther(Notice.SELF_DELETE.redirectTo(ADMIN_USERS_PATH)).build();

        } catch (IllegalStateException ex) {
            return Response.seeOther(Notice.LAST_ADMIN.redirectTo(ADMIN_USERS_PATH)).build();

        } catch (EntityNotFoundException ex) {
            return Response.seeOther(Notice.NOT_FOUND.redirectTo(ADMIN_USERS_PATH)).build();
        }
    }

}
