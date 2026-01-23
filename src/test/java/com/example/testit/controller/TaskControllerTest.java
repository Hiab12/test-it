package com.example.testit.controller;

import com.example.testit.adapter.user.CurrentUserService;
import com.example.testit.model.Task;
import com.example.testit.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean TaskService taskService;
    @MockBean CurrentUserService currentUserService;

    @Test
    void startTask_returns200_whenOk() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(7L));
        when(taskService.startTask(1L, 7L)).thenReturn(new Task());

        mockMvc.perform(post("/tasks/1/start"))
                .andExpect(status().isOk());
    }

    @Test
    void startTask_returns400_whenServiceThrows() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(Optional.of(7L));
        when(taskService.startTask(1L, 7L)).thenThrow(new IllegalStateException("bad"));

        mockMvc.perform(post("/tasks/1/start"))
                .andExpect(status().isBadRequest());
    }
}
