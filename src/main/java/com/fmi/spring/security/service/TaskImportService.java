package com.fmi.spring.security.service;

import com.fmi.spring.security.dto.ImportResult;
import com.fmi.spring.security.exception.FileValidationException;
import com.fmi.spring.security.model.Status;
import com.fmi.spring.security.model.Task;
import com.fmi.spring.security.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskImportService {

    private static final int MAGIC_SAMPLE_SIZE = 4096;
    private static final Tika TIKA = new Tika();
    public static final String DESCRIPTION = "description";
    public static final String DUEDATE = "duedate";
    public static final String STATUS = "status";
    public static final String TITLE = "title";

    private final UserService userService;
    private final TaskService taskService;

    @Transactional
    public ImportResult importFromCsv(InputStream csvStream, String originalFilename, String username) {

        User owner = userService.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        InputStream safeStream = validateCsvStream(csvStream, originalFilename);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(safeStream))) {

            Map<String, Integer> headerNameMap = extractAndValidateHeaders(reader);

            ImportResult result = new ImportResult();
            result.setFilename(originalFilename);

            createTasksFromFile(reader, headerNameMap, owner, result);

            result.setSuccess(result.getImportedCount() > 0 && !result.hasErrors());
            result.setSummary("Imported " + result.getImportedCount() + " task(s). Errors: " + result.getErrors().size());

            return result;

        } catch (IOException e) {
            throw new FileValidationException("Failed to read CSV file.", e);
        }
    }

    private static Map<String, Integer> extractAndValidateHeaders(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new FileValidationException("File is empty.");
        }

        String[] headers = headerLine.split(",", -1);
        Map<String, Integer> headerNameMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerNameMap.put(headers[i].trim().toLowerCase(), i);
        }

        if (!headerNameMap.containsKey(TITLE)) {
            throw new FileValidationException("CSV must contain at least a 'title' column.");
        }
        return headerNameMap;
    }

    private void createTasksFromFile(BufferedReader reader, Map<String, Integer> index, User owner, ImportResult result) throws IOException {
        String line;
        int row = 1;

        while ((line = reader.readLine()) != null) {
            row++;
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] cols = line.split(",", -1);
            try {
                Task task = parseTaskRow(cols, index, owner, row);
                taskService.saveTask(task);
                result.setImportedCount(result.getImportedCount() + 1);
            } catch (IllegalArgumentException e) {
                result.addError("Row " + row + ": " + e.getMessage());
            }
        }
    }

    private InputStream validateCsvStream(InputStream originalStream, String originalFilename) {

        if (originalStream == null) {
            throw new FileValidationException("No file data provided.");
        }

        // Wrap so we can "peek"
        PushbackInputStream pushbackStream = new PushbackInputStream(originalStream, MAGIC_SAMPLE_SIZE);

        byte[] buffer = new byte[MAGIC_SAMPLE_SIZE];
        int read;
        try {
            read = pushbackStream.read(buffer);
        } catch (IOException e) {
            throw new FileValidationException("Unable to read uploaded file.", e);
        }

        if (read == -1) {
            throw new FileValidationException("Uploaded file is empty.");
        }

        // Give bytes back so downstream readers see the full stream
        try {
            pushbackStream.unread(buffer, 0, read);
        } catch (IOException e) {
            throw new FileValidationException("Unable to reset uploaded file stream.", e);
        }

        validateMimeType(originalFilename, buffer);
        validateNoNullBytes(read, buffer);
        validateComaSeparatedText(buffer, read);
        return pushbackStream;
    }

    private static void validateMimeType(String originalFilename, byte[] buffer) {
        //  Detect MIME from content + filename
        String detected = TIKA.detect(buffer, originalFilename);
        MediaType mediaType = MediaType.parse(detected);

        if (mediaType == null) {
            throw new FileValidationException("Unrecognized file type.");
        }

        String type = mediaType.getType();   // e.g. "text"
        String subtype = mediaType.getSubtype(); // e.g. "csv" or "plain"

        // Accept typical CSV-ish types and plain text
        boolean isAcceptableMime = "text".equalsIgnoreCase(type) && ("csv".equalsIgnoreCase(subtype) || "plain".equalsIgnoreCase(subtype) || "comma-separated-values".equalsIgnoreCase(subtype) || "tab-separated-values".equalsIgnoreCase(subtype) || "x-csv".equalsIgnoreCase(subtype));

        // Also accept application/octet-stream IF filename ends with .csv
        if (!isAcceptableMime && "application".equalsIgnoreCase(type) && "octet-stream".equalsIgnoreCase(subtype)) {
            if (originalFilename != null && originalFilename.toLowerCase().endsWith(".csv")) {
                isAcceptableMime = true; // This is the most common case!
            }
        }

        if ("application".equalsIgnoreCase(type) && "vnd.ms-excel".equalsIgnoreCase(subtype)) {
            isAcceptableMime = true;
        }
    }

    private static void validateNoNullBytes(int read, byte[] buffer) {
        //  Sanity: must be text (no null bytes in first 8KB)
        for (int i = 0; i < read; i++) {
            if (buffer[i] == 0) {
                throw new FileValidationException("File is not a valid text/CSV file.");
            }
        }
    }

    private static void validateComaSeparatedText(byte[] buffer, int read) {
        // Ensure first line looks like comma-separated text
        String sample = new String(buffer, 0, read, java.nio.charset.StandardCharsets.UTF_8);
        String firstLine = sample.lines().findFirst().orElse("");
        if (!firstLine.contains(",")) {
            throw new FileValidationException("CSV header line not detected (missing comma).");
        }
    }

    private Task parseTaskRow(String[] cols, Map<String, Integer> index, User owner, int row) {

        Task task = new Task();
        task.setOwner(owner);

        // title (required)
        String title = getColumn(cols, index.get(TITLE));
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        task.setTitle(title);

        // description (optional)
        if (index.containsKey(DESCRIPTION)) {
            task.setDescription(getColumn(cols, index.get(DESCRIPTION)));
        }

        // dueDate (format: YYYY-MM-DD)
        if (index.containsKey(DUEDATE)) {
            String due = getColumn(cols, index.get(DUEDATE));
            if (due != null && !due.isBlank()) {
                try {
                    task.setDueDate(LocalDate.parse(due.trim()));
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid dueDate format, expected YYYY-MM-DD.");
                }
            }
        }

        // status defaults to PENDING
        Status status = Status.PENDING;
        if (index.containsKey(STATUS)) {
            String s = getColumn(cols, index.get(STATUS));
            if (s != null && !s.isBlank()) {
                try {
                    status = Status.valueOf(s.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid status value: " + s);
                }
            }
        }
        task.setStatus(status);

        return task;
    }

    private String getColumn(String[] cols, Integer idx) {
        if (idx == null || idx < 0 || idx >= cols.length) return null;
        return cols[idx].trim();
    }
}
