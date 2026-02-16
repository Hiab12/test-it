package com.example.testit.controller;

import com.example.testit.model.Task;
import com.example.testit.model.User;
import com.example.testit.repository.UserRepository;
import com.example.testit.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService,
                          UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
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
    public Task createTask(@RequestBody TaskRequest request,
                           Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String username = authentication.getName();

        User requester = userRepository.findByUsername(username);
        if (requester == null) {
            throw new IllegalStateException("User not found");
        }

        Long requesterId = requester.getId();
        Long assignedId = request.userId != null ? request.userId : requesterId;

        return taskService.createTask(request.title,
                request.description,
                requesterId,
                assignedId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
                                           @RequestBody Task task) {
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
    public ResponseEntity<Task> startTask(@PathVariable Long id,
                                          Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        Task task = taskService.startTask(id, user.getId());
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<Task> finishTask(@PathVariable Long id,
                                           Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        Task task = taskService.finishTask(id, user.getId());
        return ResponseEntity.ok(task);
    }

    public static class TaskRequest {
        public String title;
        public String description;
        public Long userId;
    }
}
