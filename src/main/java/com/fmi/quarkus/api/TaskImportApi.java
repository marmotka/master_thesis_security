package com.fmi.quarkus.api;

import com.fmi.quarkus.dto.ImportResult;
import com.fmi.quarkus.service.TaskImportService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Path("/api/tasks")
@Authenticated
public class TaskImportApi {

    @Inject TaskImportService importService;
    @Inject
    SecurityIdentity identity;

    @POST
    @Path("import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importCsv(@MultipartForm MultipartFormDataInput input) throws IOException {
        File uploadFile = input.getFormDataPart("file", File.class, null);
        if (uploadFile == null) {
            return Response.status(400).entity(Map.of("error", "No file uploaded")).build();
        }

        String username = identity.getPrincipal().getName();
        ImportResult result = importService.importFromCsv(uploadFile, username);

        return Response.ok(result).build();
    }
}