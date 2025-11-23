package com.fmi.spring.security.controller.api;

import com.fmi.spring.security.controller.api.dto.TaskImportApiResponse;
import com.fmi.spring.security.dto.ImportResult;
import com.fmi.spring.security.exception.FileValidationException;
import com.fmi.spring.security.service.TaskImportService;
import com.fmi.spring.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskImportApiController {

    private final TaskImportService taskImportService;
    private final UserService userService;


    @PostMapping(
            path = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TaskImportApiResponse> importTasksApi(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "owner", required = false) String ownerParam,
            Principal principal,
            Authentication authentication) {  // needed to check if user is admin

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(TaskImportApiResponse.error(file != null ? file.getOriginalFilename() : null,
                            "Please provide a non-empty CSV file."));
        }

        String currentUsername = principal.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));


        String targetOwner;
        if (ownerParam != null && !ownerParam.isBlank()) {
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(TaskImportApiResponse.error(file.getOriginalFilename(),
                                "Only admins can import tasks for other users."));
            }
            targetOwner = ownerParam.trim();
        } else {
            targetOwner = currentUsername; // regular user or admin importing for himself
        }

        if (!userService.existsByUsername(targetOwner)) {
            return ResponseEntity.badRequest()
                    .body(TaskImportApiResponse.error(file.getOriginalFilename(),
                            "User '" + targetOwner + "' does not exist."));
        }

        try {
            ImportResult result = taskImportService.importFromCsv(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    targetOwner
            );

            String message = result.hasErrors()
                    ? "Imported " + result.getImportedCount() + " tasks for user '" + targetOwner +
                    "', but " + result.getErrors().size() + " row(s) failed."
                    : "Successfully imported " + result.getImportedCount() + " tasks for user '" + targetOwner + "'.";

            TaskImportApiResponse response = new TaskImportApiResponse(
                    true,
                    result.getFilename(),
                    result.getImportedCount(),
                    result.getErrors(),
                    message
            );

            return ResponseEntity.ok(response);
        } catch (FileValidationException ex) {
            return ResponseEntity.badRequest()
                    .body(TaskImportApiResponse.error(file.getOriginalFilename(), ex.getMessage()));

        } catch (Exception ex) {
            log.error("API task import failed for target user {} (requested by {})", targetOwner, currentUsername, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TaskImportApiResponse.error(file.getOriginalFilename(),
                            "Import failed due to an internal error."));
        }
    }
}
