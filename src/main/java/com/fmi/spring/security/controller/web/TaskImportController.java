package com.fmi.spring.security.controller.web;

import com.fmi.spring.security.dto.ImportResult;
import com.fmi.spring.security.exception.FileValidationException;
import com.fmi.spring.security.service.TaskImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskImportController {

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String RESULT = "result";
    public static final String TASKS_IMPORT = "tasks_import";

    private final TaskImportService taskImportService;


    @GetMapping("/import")
    public String showImportForm(Model model) {
        if (!model.containsAttribute(RESULT)) {
            model.addAttribute(RESULT, null);
        }
        return TASKS_IMPORT;
    }

    @PostMapping("/import")
    public String handleImport(@RequestParam("file") MultipartFile file,
                               Authentication auth,
                               Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute(ERROR_MESSAGE, "Please select a CSV file.");
            return TASKS_IMPORT;
        }

        String username = auth.getName();

        try {
            ImportResult result = taskImportService.importFromCsv(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    username
            );
            model.addAttribute(RESULT, result);

            if (result.hasErrors()) {
                model.addAttribute(ERROR_MESSAGE,
                        "File processed with some errors. See details below.");
            }

        } catch (FileValidationException e) {
            model.addAttribute(ERROR_MESSAGE, e.getMessage());
        } catch (IOException e) {
            model.addAttribute(ERROR_MESSAGE, "Could not read uploaded file.");
        } catch (Exception e) {
            log.error("Import failed", e);
            model.addAttribute(ERROR_MESSAGE, "Import failed due to an internal error.");
        }

        return TASKS_IMPORT;
    }
}
