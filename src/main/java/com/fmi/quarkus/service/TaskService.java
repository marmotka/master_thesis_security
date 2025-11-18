package com.fmi.quarkus.service;

import com.fmi.quarkus.dto.TaskForm;
import com.fmi.quarkus.exception.TaskAccessException;
import com.fmi.quarkus.exception.TaskValidationException;
import com.fmi.quarkus.model.Status;
import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.model.User;
import com.fmi.quarkus.util.TaskFields;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@ApplicationScoped
public class TaskService {

    private static final Logger log = Logger.getLogger(TaskService.class);

    public List<Task> listTasksForUser(String username) {
        User owner = findUserOrThrow(username);

        return Task.list("owner", owner)
                .stream()
                .map(Task.class::cast)
                .sorted(Comparator
                        .comparingInt((Task task) -> Status.priorityOf(task.getStatus()))
                        .thenComparing(Task::getDueDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(t -> t.getTitle() == null ? "" : t.getTitle(),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Task getTaskForUser(String username, Long taskId) {
        User owner = findUserOrThrow(username);
        Task task = Task.findById(taskId);
        if (task == null) {
            throw new TaskAccessException("Task not found.");
        }
        if (!Objects.equals(task.owner.id, owner.id)) {
            throw new TaskAccessException("You are not allowed to view this task.");
        }
        return task;
    }

    @Transactional
    public Task saveTaskForUser(String username, TaskForm form) {
        Map<String, String> errors = validate(form);

        if (!errors.isEmpty()) {
            throw new TaskValidationException("Invalid task data", errors);
        }
        User owner = findUserOrThrow(username);

        if (form.id == null) {
            // Create
            Task t = new Task();
            updateFields(form, t, owner);
            t.persist();
            log.infof("Task created for user %s: %s", username, t.title);
            return t;
        } else {
            // Update
            Task t = Task.findById(form.id);
            if (t == null) {
                throw new TaskAccessException("Task not found.");
            }
            if (!Objects.equals(t.owner.id, owner.id)) {
                throw new TaskAccessException("You are not allowed to update this task.");
            }
            updateFields(form, t, owner);
            t.persist();
            log.infof("Task %d updated by user %s", t.id, username);
            return t;
        }
    }


    @Transactional
    public void deleteTaskForUser(String username, Long taskId) {
        User owner = findUserOrThrow(username);
        Task t = Task.findById(taskId);
        if (t == null) {
            throw new TaskAccessException("Task not found.");
        }
        if (!Objects.equals(t.owner.id, owner.id)) {
            throw new TaskAccessException("You are not allowed to delete this task.");
        }
        t.delete();
        log.infof("Task %d deleted by user %s", t.id, username);
    }


    private static void updateFields(TaskForm form, Task t, User owner) {
        t.title = form.title.trim();
        t.description = form.description != null ? form.description.trim() : null;
        t.dueDate = (form.dueDate == null || form.dueDate.isBlank()) ? null : LocalDate.parse(form.dueDate);
        t.status = Status.valueOf(form.status);
        t.owner = owner;
    }

    private User findUserOrThrow(String username) {
        User owner = User.find("username", username).firstResult();
        if (owner == null) {
            log.warnf("User not found while operating on tasks: %s", username);
            throw new TaskAccessException("User not found.");
        }
        return owner;
    }

    private Map<String, String> validate(TaskForm form) {
        Map<String, String> errors = new HashMap<>();

        if (form.title == null || form.title.isBlank()) {
            errors.put(TaskFields.TITLE, "Title is required.");
        } else if (form.title.length() > 255) {
            errors.put(TaskFields.TITLE, "Title is too long (max 255 characters).");
        }

        if (form.dueDate != null && !form.dueDate.isBlank()) {
            try {
                LocalDate.parse(form.dueDate);
            } catch (DateTimeParseException ex) {
                errors.put(TaskFields.DUE_DATE, "Invalid date format.");
            }
        }

        if (form.status == null || form.status.isBlank()) {
            errors.put(TaskFields.STATUS, "Status is required.");
        } else {
            try {
                Status.valueOf(form.status);
            } catch (IllegalArgumentException ex) {
                errors.put(TaskFields.STATUS, "Invalid status.");
            }
        }

        return errors;
    }

}