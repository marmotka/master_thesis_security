package com.fmi.quarkus.dto;

public class TaskForm {

    public Long id;          // null for create; non-null for update
    public String title;
    public String description;
    public String dueDate;   // ISO string from <input type="date">
    public String status;    // "PENDING", "IN_PROGRESS", "DONE"

    public TaskForm() {}

    public TaskForm(Long id, String title, String description, String dueDate, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }
}
