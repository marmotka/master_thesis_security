package com.fmi.quarkus.api;

import com.fmi.quarkus.dto.ImportResult;
import com.fmi.quarkus.exception.FileValidationException;
import com.fmi.quarkus.service.TaskImportService;
import com.fmi.quarkus.util.CommonNames;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/api/tasks")
@Blocking
@Produces(MediaType.APPLICATION_JSON)
public class TaskImportApiResource {

    private static final Logger log = Logger.getLogger(TaskImportApiResource.class);
    public static final String UNKNOWN_CSV = "unknown.csv";

    @Inject
    SecurityIdentity identity;

    @Inject
    TaskImportService importService;

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({CommonNames.USER, CommonNames.ADMIN})
    public Response importTasks(@QueryParam("owner") String ownerUsername,
                                MultipartFormDataInput input) {

        if (input == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error("No file uploaded."))
                    .build();
        }


        String caller = identity.getPrincipal().getName();

        // Determine effective owner:
        // - Normal users can only import for themselves
        // - Admins can optionally specify ?owner=username
        String effectiveOwner = resolveOwner(caller, ownerUsername);

        String originalFilename = extractOriginalFilename(input);

        ImportResult result;
        try (InputStream in = input.getFormDataPart(CommonNames.FILE, InputStream.class, null)) {
            if (in == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(error("No file data found."))
                        .build();
            }

            result = importService.importFromCsv(in, originalFilename, effectiveOwner);
            result.setSuccess(true);

            return Response.ok(result).build();

        } catch (FileValidationException ve) {
            log.warnf("CSV import validation error by '%s': %s", caller, ve.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error(ve.getMessage()))
                    .build();

        } catch (IllegalArgumentException iae) {
            log.warnf("CSV import access/validation error by '%s': %s", caller, iae.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error(iae.getMessage()))
                    .build();

        } catch (Exception e) {
            log.errorf(e, "Unexpected error during CSV import by '%s'", caller);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error("Import failed due to server error."))
                    .build();
        }
    }


    private String resolveOwner(String caller, String ownerUsername) {
        if (ownerUsername != null && !ownerUsername.isBlank()
                && identity.getRoles().contains(CommonNames.ADMIN)) {
            return ownerUsername;
        }
        return caller;
    }


    private String extractOriginalFilename(MultipartFormDataInput input) {
        try {
            Map<String, List<org.jboss.resteasy.plugins.providers.multipart.InputPart>> formParts =
                    input.getFormDataMap();

            List<org.jboss.resteasy.plugins.providers.multipart.InputPart> files = formParts.get(CommonNames.FILE);
            if (files == null || files.isEmpty()) {
                return UNKNOWN_CSV;
            }
            var headers = files.get(0).getHeaders();
            String cd = headers.getFirst("Content-Disposition");
            if (cd != null) {
                for (String part : cd.split(";")) {
                    part = part.trim();
                    if (part.startsWith("filename=")) {
                        String filename = part.substring("filename=".length()).trim();
                        return filename.replace("\"", "");
                    }
                }
            }
            return UNKNOWN_CSV;
        } catch (Exception e) {
            log.debug("Failed to extract filename from multipart", e);
            return UNKNOWN_CSV;
        }
    }

    private Map<String, Object> error(String message) {
        return Map.of(
                "success", false,
                "message", message
        );
    }
}
