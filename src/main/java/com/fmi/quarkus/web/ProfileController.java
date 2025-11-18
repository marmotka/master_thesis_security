package com.fmi.quarkus.web;

import com.fmi.quarkus.exception.PasswordChangeException;
import com.fmi.quarkus.service.ChangePasswordRequest;
import com.fmi.quarkus.service.UserService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

@Path("/profile")
@Blocking
@RolesAllowed({"USER", "ADMIN"})
public class ProfileController {


    public static final String BLANK_STRING = "";
    @Inject
    SecurityIdentity identity;
    @Inject
    UserService userService;

    @CheckedTemplate
    public static class Tpl {
        public static native TemplateInstance profile(
                String username,
                String email,
                boolean openPasswordSection,
                Map<String, String> passwordErrors,
                SecurityIdentity identity
        );
    }


    @GET
    @Path("/view")
    public TemplateInstance view(@QueryParam("notice") @DefaultValue(BLANK_STRING) String notice) {
        String username = identity.getPrincipal().getName();
        var user = userService.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean openSection = false;
        String successMessage = BLANK_STRING;

        if ("pwChanged".equals(notice)) {
            successMessage = "Password updated successfully.";
        }

        return Tpl.profile(user.username, user.email, openSection, Map.of(), identity)
                .data("successMessage", successMessage)
                .data("errorMessage", BLANK_STRING);
    }

    // Change password
    public static class ChangePasswordForm {
        @FormParam("currentPassword")
        public String currentPassword;
        @FormParam("newPassword")
        public String newPassword;
        @FormParam("confirmPassword")
        public String confirmPassword;
    }

    @POST
    @Path("/password")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object changePassword(@BeanParam ChangePasswordForm form) {

        String username = identity.getPrincipal().getName();
        var dto = new ChangePasswordRequest(
                form.currentPassword,
                form.newPassword,
                form.confirmPassword
        );

        try {
            userService.changePassword(username, dto);
            // PRG pattern – no resubmit on refresh, no passwords in URL
            return Response.seeOther(URI.create("/profile/view?notice=pwChanged")).build();

        } catch (PasswordChangeException ex) {
            var user = userService.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            // Re-render profile with password section open and field errors
            return Tpl.profile(
                    user.username,
                    user.email,
                    true,
                    ex.getFieldErrors(),
                    identity
            );
        }
    }
}