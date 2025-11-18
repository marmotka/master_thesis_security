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
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

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

        // Extract original filename from multipart metadata
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

        // Render page with result – no redirect.
        return Tpl.importForm(result, identity);
    }



//    @POST
//    @Path("import")
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response upload(@MultipartForm MultipartFormDataInput input) throws IOException {
//        File file = input.getFormDataPart("file", File.class, null);
//        if (file == null) {
//            return redirectWithError("No file uploaded.");
//        }
//        String username = identity.getPrincipal().getName();
//        ImportResult result;
//        try {
//            result = importService.importFromCsv(file, username);
//        } catch (Exception e) {
//            return redirectWithError("Invalid file: " + e.getMessage());
//        }
//
//        return Response.seeOther(URI.create("/tasks/import"))
//                .entity(Tpl.importForm(result, identity).data("filename", file))
//                .build();
//    }



    private Response redirectWithError(String msg) {
        ImportResult err = new ImportResult(0, List.of(msg));
        TemplateInstance page = Tpl.importForm(err, identity);
        return Response.seeOther(URI.create("/tasks/import")).entity(page).build();
    }

}