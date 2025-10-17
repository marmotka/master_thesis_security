package com.fmi.spring.security.controller;

import com.fmi.spring.security.model.Task;
import com.fmi.spring.security.service.TaskService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getTasks(Authentication auth) {
        return taskService.getTasksForUser(auth.getName());
    }

    @PostMapping
    public Task createTask(@RequestBody Task task, Authentication auth) {
        return taskService.createTask(task, auth.getName());
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task, Authentication auth) {
        return taskService.updateTask(id, task, auth.getName());
    }

     @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id, Authentication auth) {
        taskService.deleteTask(id, auth.getName());
    }
}
