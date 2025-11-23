package com.fmi.spring.security.service;

import com.fmi.spring.security.model.Status;
import com.fmi.spring.security.model.Task;
import com.fmi.spring.security.model.User;
import com.fmi.spring.security.repository.TaskRepository;
import com.fmi.spring.security.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getTasksForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return taskRepository.findByOwner(user)
            .stream()
                .map(Task.class::cast)
                .sorted(Comparator
                        .comparingInt((Task t) -> getStatusPriority(t.getStatus()))
                        .thenComparing(Task::getDueDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(t -> t.getTitle() == null ? "" : t.getTitle(),
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Task createTask(Task task, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        task.setOwner(user);
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task updatedTask, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to update this task");
        }

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setDueDate(updatedTask.getDueDate());
        task.setStatus(updatedTask.getStatus());

        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to delete this task");
        }

        taskRepository.delete(task);
    }

    public void saveTask(Task task){
        taskRepository.save(task);
    }

        private int getStatusPriority(Status status) {
        if (status == null) {
            return 4;
        }
        return switch (status.name()) {
            case "IN_PROGRESS"   -> 1;
            case "PENDING"       -> 2;
            case "DONE"          -> 3;
            default              -> 4;
        };
    }
}

