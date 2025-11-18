package com.fmi.quarkus.model;

public enum Status {
    IN_PROGRESS(1),
    PENDING(2),
    DONE(3),
    UNKNOWN(4);   // optional fallback

    private final int priority;

    Status(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    // Optional: safe method if status can be null
    public static int priorityOf(Status status) {
        return status != null ? status.priority : UNKNOWN.priority;
    }
}