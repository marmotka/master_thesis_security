package com.fmi.quarkus.dto;

public class TaskForm {

    public Long id;
    public String title;
    public String description;
    public String dueDate;
    public String status;

    public TaskForm() {
    }

    public TaskForm(Long id, String title, String description, String dueDate, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }
}
