package com.fmi.spring.security.controller.api.dto;

import java.util.List;

public class TaskImportApiResponse {

    private boolean success;
    private String filename;
    private int importedCount;
    private List<String> errors;
    private String message;

    public TaskImportApiResponse(boolean success,
                                 String filename,
                                 int importedCount,
                                 List<String> errors,
                                 String message) {
        this.success = success;
        this.filename = filename;
        this.importedCount = importedCount;
        this.errors = errors;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFilename() {
        return filename;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getMessage() {
        return message;
    }
}
