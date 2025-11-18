package com.fmi.quarkus.web;

import com.fmi.quarkus.dto.ImportResult;
import com.fmi.quarkus.exception.FileValidationException;
import com.fmi.quarkus.service.TaskImportService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.io.InputStream;

@Path("/tasks")
@Authenticated
public class TaskImportController {

    @Inject
    TaskImportService importService;
    @Inject
    SecurityIdentity identity;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Tpl {
        public static native TemplateInstance importForm(ImportResult result,
                                                         SecurityIdentity identity);
    }

    @GET
    @Path("import")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showForm() {
        return Tpl.importForm(null, identity);
    }

    @POST
    @Path("import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TemplateInstance uploadCsv(MultipartFormDataInput input) throws IOException {
        if (input == null) {
            return Tpl.importForm(null, identity)
                    .data("errorMessage", "No file uploaded.");
        }

        String username = identity.getPrincipal().getName();

        String originalFilename = importService.extractOriginalFilename(input);

        ImportResult result;
        try (InputStream in = input.getFormDataPart("file", InputStream.class, null)) {
            if (in == null) {
                return Tpl.importForm(null, identity)
                        .data("errorMessage", "No file data found.");
            }
            result = importService.importFromCsv(in, originalFilename, username);
        } catch (FileValidationException ve) {
            return Tpl.importForm(null, identity)
                    .data("errorMessage", ve.getMessage());
        } catch (Exception e) {
            return Tpl.importForm(null, identity)
                    .data("errorMessage", "Import failed: " + e.getMessage());
        }

        return Tpl.importForm(result, identity);
    }

}