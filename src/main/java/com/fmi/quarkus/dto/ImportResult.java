package com.fmi.quarkus.dto;

import java.util.List;

public record ImportResult(int importedCount, List<String> errors) {
}