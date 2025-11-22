//package com.fmi.spring.security.controller.api;
//
//import com.fmi.spring.security.dto.TaskDto;
//import com.fmi.spring.security.model.Task;
//import com.fmi.spring.security.service.TaskService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/tasks")
//public class TaskRestController {
//
//    @Autowired
//    private TaskService taskService;
//
//    @GetMapping
//    public List<TaskDto> getTasks(Authentication auth) {
//        return taskService.getTasksForUser(auth.getName())
//                .stream().map(TaskRestController::toDto).toList();
//    }
//
//    @PostMapping
//    public ResponseEntity<TaskDto> createTask(@RequestBody @Valid TaskDto dto, Authentication auth) {
//        Task created = taskService.createTask(fromDto(dto), auth.getName());
//        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
//    }
//
//    @PutMapping("/{id}")
//    public TaskDto updateTask(@PathVariable Long id, @RequestBody @Valid TaskDto dto, Authentication auth) {
//        return toDto(taskService.updateTask(id, fromDto(dto), auth.getName()));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication auth) {
//        taskService.deleteTask(id, auth.getName());
//        return ResponseEntity.noContent().build();
//    }
//
//    private static TaskDto toDto(Task t) {
////        todo /* map entity -> dto */
//        return TaskDto.builder().build();
//    }
//
//    private static Task fromDto(TaskDto d) {
//        //todo /* map dto -> entity */
//        return new Task();
//    }
//}
