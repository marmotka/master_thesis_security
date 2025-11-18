package com.fmi.quarkus.util;

import java.net.URI;

public enum Notice {
    DELETED("User deleted successfully.", "success"),
    SELF_DELETE("You cannot delete your own account.", "error"),
    NOT_FOUND("User not found.", "error"),
    LAST_ADMIN("Cannot delete the last admin user.", "error");

    private final String message;
    private final String type; // "success" or "error"

    Notice(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String message() { return message; }
    public String type() { return type; }

    // Helper to create redirect URL
    public URI redirectTo(String basePath) {
        return URI.create(basePath + "?notice=" + name().toLowerCase());
    }
}
