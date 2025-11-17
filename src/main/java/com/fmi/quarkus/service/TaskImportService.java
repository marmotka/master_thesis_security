package com.fmi.quarkus.service;

import com.fmi.quarkus.dto.ImportResult;
import com.fmi.quarkus.model.Status;
import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.model.User;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TaskImportService {

    @Inject UserService userService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Import tasks from CSV file.
     * @param file Uploaded file (CSV)
     * @param username Owner of all imported tasks
     * @return Import result with stats
     */
    @Transactional
    public ImportResult importFromCsv(File file, String username) throws IOException {
        var owner = userService.findByUsername(username);
        if (owner.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        Path tempPath = file.toPath();
        validateFile(tempPath);

        List<Task> validTasks = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(tempPath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    Task task = parseTask(record, owner.get());
                    if (task != null) {
                        validTasks.add(task);
                    }
                } catch (Exception e) {
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }

        // Persist all valid tasks atomically
        if (!validTasks.isEmpty()) {
            Task.persist(validTasks);
        }

        return new ImportResult(validTasks.size(), errors);
    }

    private void validateFile(Path path) throws IOException {
        if (!Files.exists(path) || Files.size(path) == 0) {
            throw new IllegalArgumentException("File is empty or missing.");
        }
        if (Files.size(path) > 1_000_000) {
            throw new IllegalArgumentException("File too large (max 1MB).");
        }

        // 1. MIME type check
        String detectedType = Files.probeContentType(path);
        if (detectedType == null || !detectedType.equals("text/csv")) {
            // Fallback: allow text/plain (common for CSV)
            if (!"text/plain".equals(detectedType)) {
                throw new IllegalArgumentException("Invalid file type. Only CSV files are allowed.");
            }
        }

        // 2. Check first few bytes are ASCII text (no binary)
        try (InputStream is = Files.newInputStream(path)) {
            byte[] header = new byte[512];
            int read = is.read(header);
            if (read > 0) {
                for (int i = 0; i < read; i++) {
                    if (header[i] < 0x09 && header[i] != 0x0A && header[i] != 0x0D) {
                        // Non-printable, non-control → likely binary
                        throw new IllegalArgumentException("File contains binary data. Only text CSV allowed.");
                    }
                }
            }
        }

        // 3. Try to parse first line as CSV header
        try (Reader reader = Files.newBufferedReader(path);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            if (!parser.getHeaderMap().containsKey("title")) {
                throw new IllegalArgumentException("CSV must contain 'title' column.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid CSV format: " + e.getMessage());
        }

        // Optional: scan for malicious content (e.g., ClamAV)
        // scanWithClamAV(path);
    }

    private Task parseTask(CSVRecord record, User owner) {
        String title = getRequired(record, "title");
        String description = getOptional(record, "description", "");
        String dueDateStr = getOptional(record, "dueDate", null);
        String statusStr = getOptional(record, "status", "PENDING");

        LocalDate dueDate = null;
        if (dueDateStr != null && !dueDateStr.isBlank()) {
            try {
                dueDate = LocalDate.parse(dueDateStr.trim(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid dueDate format: " + dueDateStr);
            }
        }

        Status status;
        try {
            status = Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr + " (use PENDING, IN_PROGRESS, DONE)");
        }

        Task task = new Task();
        task.title = title;
        task.description = description;
        task.dueDate = dueDate;
        task.status = status;
        task.owner = owner;

        return task;
    }

    private String getRequired(CSVRecord r, String header) {
        String val = r.get(header);
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + header);
        }
        return val.trim();
    }

    private String getOptional(CSVRecord r, String header, String defaultValue) {
        String val = r.get(header);
        return (val == null || val.isBlank()) ? defaultValue : val.trim();
    }

    //todo integrate ClamAV
    // private void scanWithClamAV(Path path) throws IOException { ... }
}