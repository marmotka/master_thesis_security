package com.fmi.spring.security.controller.api;

import com.fmi.spring.security.controller.api.dto.TaskImportApiResponse;
import com.fmi.spring.security.dto.ImportResult;
import com.fmi.spring.security.exception.FileValidationException;
import com.fmi.spring.security.service.TaskImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskImportApiController {

    private final TaskImportService taskImportService;

    @PostMapping(
            path = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<TaskImportApiResponse> importTasksApi(
            @RequestPart("file") MultipartFile file,
            Principal principal) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new TaskImportApiResponse(false, null, 0, null, "Please provide a non-empty CSV file.")
            );
        }

        String username = principal.getName();

        try {
            ImportResult result = taskImportService.importFromCsv(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    username
            );

            String message;
            if (result.hasErrors()) {
                message = "Imported " + result.getImportedCount()
                        + " tasks, but some rows failed (" + result.getErrors().size() + ").";
            } else {
                message = "Successfully imported " + result.getImportedCount() + " tasks.";
            }

            TaskImportApiResponse body = new TaskImportApiResponse(
                    result.isSuccess(),
                    result.getFilename(),
                    result.getImportedCount(),
                    result.getErrors(),
                    message
            );

            return ResponseEntity.ok(body);

        } catch (FileValidationException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new TaskImportApiResponse(
                            false,
                            file.getOriginalFilename(),
                            0,
                            null,
                            ex.getMessage()
                    ));

        } catch (Exception ex) {
            log.error("API task import failed for user {}", username, ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TaskImportApiResponse(
                            false,
                            file.getOriginalFilename(),
                            0,
                            null,
                            "Import failed due to an internal error."
                    ));
        }
    }
}
