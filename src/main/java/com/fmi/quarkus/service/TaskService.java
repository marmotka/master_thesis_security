package com.fmi.quarkus.service;


import com.fmi.quarkus.model.Task;
import com.fmi.quarkus.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class TaskService {

    public List<Task> listFor(User owner) {
        return Task.list("owner", owner);
    }

    @Transactional
    public Task create(User owner, String title, String description, String dueDateIso, String status) {
        Task t = new Task();
        t.title = title;
        t.description = description;
        if (dueDateIso != null && !dueDateIso.isBlank()) {
            t.dueDate = java.time.LocalDate.parse(dueDateIso);
        }
        if (status != null) {
            t.status = Task.Status.valueOf(status);
        }
        t.owner = owner;
        t.persist();
        return t;
    }

    @Transactional
    public void update(Long id, String title, String description, String dueDateIso, String status, User caller) {
        Task t = Task.findById(id);
        if (t == null || !t.owner.id.equals(caller.id)) throw new IllegalArgumentException("Not found");
        t.title = title;
        t.description = description;
        t.dueDate = (dueDateIso == null || dueDateIso.isBlank()) ? null : java.time.LocalDate.parse(dueDateIso);
        t.status = Task.Status.valueOf(status);
        t.persist();
    }

    @Transactional
    public void delete(Long id, User caller) {
        Task t = Task.findById(id);
        if (t == null || !t.owner.id.equals(caller.id)) return;
        t.delete();
    }
}