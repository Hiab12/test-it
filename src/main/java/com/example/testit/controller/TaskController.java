package com.example.testit.controller;

import com.example.testit.adapter.user.CurrentUserService;
import com.example.testit.model.Task;
import com.example.testit.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    public TaskController(TaskService taskService, CurrentUserService currentUserService) {
        this.taskService = taskService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.findById(id);
        return task.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId) {
        return taskService.findByUserId(userId);
    }

    @PostMapping
    public Task createTask(@RequestBody TaskRequest request) {
        Long requesterId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        Long assignedId = request.userId != null ? request.userId : requesterId;
        return taskService.createTask(request.title, request.description, requesterId, assignedId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        try {
            Task updated = taskService.updateTask(task);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        try {
            Task task = taskService.startTask(id, userId);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<Task> finishTask(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        try {
            Task task = taskService.finishTask(id, userId);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    public static class TaskRequest {
        public String title;
        public String description;
        public Long userId;
    }
}
