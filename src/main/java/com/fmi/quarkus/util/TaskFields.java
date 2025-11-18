package com.fmi.quarkus.util;

import java.util.Set;

public record TaskFields() {
    public static final String TITLE       = "title";
    public static final String DESCRIPTION = "description";
    public static final String DUE_DATE    = "duedate";
    public static final String STATUS      = "status";

    public static final Set<String> REQUIRED = Set.of(TITLE);
    public static final Set<String> ALLOWED  = Set.of(TITLE, DESCRIPTION, DUE_DATE, STATUS);
}