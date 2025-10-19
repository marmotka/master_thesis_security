package com.fmi.spring.security.dto;

import java.time.LocalDate;

public record TaskDto(Long id, String title, String description, LocalDate dueDate, String status) {
    public static TaskDtoBuilder builder() {
        return new TaskDtoBuilder();
    }

    public static class TaskDtoBuilder {
        private Long id;
        private String title;
        private String description;
        private LocalDate dueDate;
        private String status;

        TaskDtoBuilder() {
        }

        public TaskDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TaskDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskDtoBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        public TaskDto build() {
            return new TaskDto(this.id, this.title, this.description, this.dueDate, this.status);
        }

        public String toString() {
            return "TaskDto.TaskDtoBuilder(id=" + this.id + ", title=" + this.title + ", description=" + this.description + ", dueDate=" + this.dueDate + ", status=" + this.status + ")";
        }
    }
}


