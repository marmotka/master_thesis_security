package com.fmi.spring.security.controller.web;

import com.fmi.spring.security.model.Task;
import com.fmi.spring.security.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;


    @GetMapping("/view")
    public String getTasks(Model model, Authentication auth) {
        var tasks = taskService.getTasksForUser(auth.getName());
        model.addAttribute("tasks", tasks);
        return "tasks"; // Looks for templates/tasks.html
    }

    // Show form for new task
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new Task());
        return "task_form"; // templates/task_form.html
    }

    // Show form for editing task
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication auth) {
        Task task = taskService.getTasksForUser(auth.getName())
                .stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Task not found"));
        model.addAttribute("task", task);
        return "task_form";
    }

    // Handle saving (same form for create & update)
    @PostMapping
    public String saveTask(@ModelAttribute Task task, Authentication auth, RedirectAttributes redirectAttributes) {
        if (task.getId() == null) {
            taskService.createTask(task, auth.getName());
            redirectAttributes.addFlashAttribute("message", "Task created successfully!");
        } else {
            taskService.updateTask(task.getId(), task, auth.getName());
            redirectAttributes.addFlashAttribute("message", "Task updated successfully!");
        }
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/tasks/view";
    }

//    @PostMapping
//    public Task createTask(@RequestBody Task task, Authentication auth) {
//        return taskService.createTask(task, auth.getName());
//    }

    @PutMapping("/edit/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task, Authentication auth) {
        return taskService.updateTask(id, task, auth.getName());
    }

     @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id, auth.getName());

         redirectAttributes.addFlashAttribute("message", "Task deleted successfully!");
         redirectAttributes.addFlashAttribute("messageType", "success");

         return "redirect:/tasks/view";
    }
}
