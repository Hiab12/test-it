package com.example.testit.controller;

import com.example.testit.adapter.user.CurrentUserService;
import com.example.testit.model.Task;
import com.example.testit.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean TaskService taskService;
    @MockBean CurrentUserService currentUserService;

    @Test
    void testGetAllTasks() throws Exception {
        when(taskService.findAll()).thenReturn(java.util.List.of(new Task()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTaskById() throws Exception {
        Task t = new Task();
        t.setId(1L);
        when(taskService.findById(1L)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCreateTask() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(7L));

        Task created = new Task();
        created.setId(10L);

        when(taskService.createTask(eq("T"), eq("D"), eq(7L), eq(7L))).thenReturn(created);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"description\":\"D\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void testStartTask() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(7L));
        when(taskService.startTask(1L, 7L)).thenReturn(new Task());

        mockMvc.perform(post("/tasks/1/start"))
                .andExpect(status().isOk());
    }
    /*
    @Test
    void testStartTaskError() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(7L));
        when(taskService.startTask(1L, 7L)).thenThrow(new IllegalStateException("bad"));

        mockMvc.perform(post("/tasks/1/start"))
                .andExpect(status().isBadRequest());
    }
    */
    @Test
    void testFinishTask() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(7L));
        when(taskService.finishTask(1L, 7L)).thenReturn(new Task());

        mockMvc.perform(post("/tasks/1/finish"))
                .andExpect(status().isOk());
    }
}
