package com.fmi.quarkus.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ImportResult {


    private String filename;
    private int importedCount = 0;
    private final List<String> errors = new ArrayList<>();
    private boolean success = false;

    public ImportResult() {
    }

    public ImportResult(int importedCount, List<String> errors) {
        this.importedCount = importedCount;
        this.errors.addAll(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getSummary() {
        if (importedCount == 0 && errors.isEmpty()) {
            return "No tasks were imported.";
        }
        return String.format("Imported %,d task%s%s",
                importedCount,
                importedCount == 1 ? "" : "s",
                hasErrors() ? " (with some errors)" : "");
    }
}