package com.fmi.spring.security.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ImportResult {

    private String filename;
    private int importedCount;
    private final List<String> errors = new ArrayList<>();
    private boolean success;
    private String summary;


    public static ImportResult error(String filename, String message) {
        ImportResult result = new ImportResult();
        result.setFilename(filename);
        result.addError(message);
        result.setSuccess(false);
        result.setSummary("Import failed: " + message);
        return result;
    }


    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addError(String message) {
        this.errors.add(message);
    }

}
