package com.fmi.quarkus.service;

import com.fmi.quarkus.dto.ImportResult;
import com.fmi.quarkus.dto.ValidationHeadersResult;
import com.fmi.quarkus.exception.FileValidationException;
import com.fmi.quarkus.model.Status;
import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskImportService {


    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String DUEDATE = "duedate";
    public static final String STATUS = "status";
    private static final Set<String> ALLOWED_HEADERS = Set.of(TITLE, DESCRIPTION, DUEDATE, STATUS);
    private static final Set<String> REQUIRED_HEADERS = Set.of(TITLE);
    private static final byte[] CSV_BOM_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Transactional
    public ImportResult importFromCsv(InputStream csvStream, String originalFilename, String username) throws IOException {

        User owner = User.findByUsername(username);
        if (owner == null) {
            throw new IllegalArgumentException("User not found");
        }

        validateFileType(csvStream, originalFilename);

        ValidationHeadersResult validationResult = getValidationHeadersResult(csvStream);

        // All validations passed → do the real import
        ImportResult result = new ImportResult();
        result.setFilename(originalFilename);
        result.setImportedCount(0);

        createTasksFromFile(validationResult, owner, result);

        return result;
    }

    private void createTasksFromFile(ValidationHeadersResult validationResult,
                                     User owner,
                                     ImportResult result) throws IOException {

        int lineNumber = 2; // assuming line 1 was the header

        try (BufferedReader reader = validationResult.reader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> values = parseCsvLine(line);

                // Pad missing columns to match header count
                while (values.size() < validationResult.headers().size()) {
                    values.add("");
                }

                try {
                    createTaskFromRow(validationResult.headers(), values, owner);
                    result.setImportedCount(result.getImportedCount() + 1);
                } catch (Exception e) {
                    result.getErrors().add("Line " + lineNumber + ": " + e.getMessage());
                }

                lineNumber++;
            }
        }
        // No need to close manually — try-with-resources does it automatically
    }

    private ValidationHeadersResult getValidationHeadersResult(InputStream csvStream) throws IOException {
        // Read first line → must be header and contain required columns
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvStream, StandardCharsets.UTF_8)); // UTF-8 always

        String headerLine = reader.readLine();
        if (headerLine == null || headerLine.trim().isEmpty()) {
            throw new FileValidationException("CSV file is empty or has no header row.");
        }

        List<String> headers = parseCsvLine(headerLine);
        Set<String> headerSet = headers.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (!headerSet.containsAll(REQUIRED_HEADERS.stream().map(String::toLowerCase).collect(Collectors.toSet()))) {
            throw new FileValidationException("Missing required column: 'title'. Found: " + String.join(", ", headers));
        }

        Set<String> unknown = new HashSet<>(headerSet);
        unknown.removeAll(ALLOWED_HEADERS.stream().map(String::toLowerCase).collect(Collectors.toSet()));
        if (!unknown.isEmpty()) {
            throw new FileValidationException("Unknown column(s): " + unknown);
        }
        return new ValidationHeadersResult(reader, headers);
    }


    private static void validateFileType(InputStream csvStream, String originalFilename) throws IOException {
        // Validate filename extension
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new FileValidationException("Only .csv files are allowed. Received: " + originalFilename);
        }

        try {
            // Check magic bytes (BOM + first chars must be text)
            csvStream.mark(1024);
            byte[] firstBytes = csvStream.readNBytes(10);
            csvStream.reset();

            // Check for UTF-8 BOM
            boolean hasBom = firstBytes.length >= 3 &&
                    firstBytes[0] == CSV_BOM_UTF8[0] &&
                    firstBytes[1] == CSV_BOM_UTF8[1] &&
                    firstBytes[2] == CSV_BOM_UTF8[2];
            // Basic text check: must contain printable ASCII or UTF-8 chars
            for (int i = hasBom ? 3 : 0; i < firstBytes.length; i++) {
                int b = firstBytes[i] & 0xFF;
                if (b < 9 || b > 13 && b < 32) {
                    // null byte or control chars (except \t \n \r)
                    throw new FileValidationException("File appears to be binary or corrupted (contains invalid control characters).");
                }
            }
        } catch (Exception e) {
            throw new FileValidationException("Wrong format. Only .csv files are allowed.");
        }

    }

    private void createTaskFromRow(List<String> headers, List<String> values, User owner) {
        Task task = new Task();
        task.owner = owner;

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).trim().toLowerCase();
            String value = i < values.size() ? values.get(i).trim() : "";

            switch (header) {
                case TITLE -> {
                    if (value.isBlank()) throw new IllegalArgumentException("Title is required");
                    task.title = value;
                }
                case DESCRIPTION -> task.description = value.isEmpty() ? null : value;
                case DUEDATE -> {
                    if (!value.isBlank()) {
                        try {
                            task.dueDate = LocalDate.parse(value); // expects YYYY-MM-DD
                        } catch (DateTimeParseException e) {
                            throw new IllegalArgumentException("Invalid dueDate format: " + value + " (use YYYY-MM-DD)");
                        }
                    }
                }
                case STATUS -> {
                    if (!value.isBlank()) {
                        try {
                            task.status = Status.valueOf(value.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid status: " + value + " (allowed: PENDING, IN_PROGRESS, DONE)");
                        }
                    }
                }
            }
        }

        task.persist();
    }

    // Simple CSV parser (handles quoted fields with commas)
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result;
    }

    // Helper to extract the real uploaded filename (e.g., "my-tasks.csv")
    public String extractOriginalFilename(MultipartFormDataInput input) throws IOException {
        try {
            // Get the "file" part and read its Content-Disposition header
            var formData = input.getFormDataMap().get("file");
            if (formData != null && !formData.isEmpty()) {
                var part = formData.get(0);
                var headers = part.getHeaders();
                var disposition = headers.getFirst("Content-Disposition");
                if (disposition != null) {
                    // Parse "filename=..." from Content-Disposition
                    var matcher = java.util.regex.Pattern.compile("filename=\"([^\"]*)\"").matcher(disposition);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception ignored) {
            // Fallback to temp filename if parsing fails
        }
        // Fallback: get temp file name
        var tempFile = input.getFormDataPart("file", java.io.File.class, null);
        return tempFile != null ? tempFile.getName() : "unknown.csv";
    }

}