//package com.fmi.spring.security.controller.api;
//
//
//import com.fmi.spring.security.dto.ImportResult;
//import com.fmi.spring.security.exception.FileValidationException;
//import com.fmi.spring.security.service.TaskImportService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestPart;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/tasks")
//@RequiredArgsConstructor
//public class TaskRestController {
//
//
//    private final TaskImportService taskImportService;
//
//    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public ResponseEntity<ImportResult> importTasksApi(@RequestPart("file") MultipartFile file, Authentication auth) {
//
//        if (file == null || file.isEmpty()) {
//            ImportResult res = ImportResult.error(null, "No file uploaded.");
//            return ResponseEntity.badRequest().body(res);
//        }
//
//        String username = auth.getName();
//
//        try {
//            ImportResult result = taskImportService.importFromCsv(file.getInputStream(), file.getOriginalFilename(), username);
//            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY;
//            return ResponseEntity.status(status).body(result);
//
//        } catch (FileValidationException e) {
//            ImportResult res = ImportResult.error(file.getOriginalFilename(), e.getMessage());
//            return ResponseEntity.badRequest().body(res);
//        } catch (IOException e) {
//            ImportResult res = ImportResult.error(file.getOriginalFilename(), "Could not read uploaded file.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
//        } catch (Exception e) {
//            ImportResult res = ImportResult.error(file.getOriginalFilename(), "Import failed: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
//        }
//    }
//}