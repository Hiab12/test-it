package com.example.testit.service;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Status;
import com.example.testit.model.Task;
import com.example.testit.model.User;
import com.example.testit.repository.TaskRepository;
import com.example.testit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock MailService mailService;

    @InjectMocks TaskService taskService;

    @Test
    void testStartTask() {
        User u = new User("hiba");
        u.setId(7L);

        Task t = new Task("T", "D", u);
        t.setId(1L);
        t.setStatus(Status.OUVERT);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));
        when(taskRepository.findByUserAndStatus(7L, Status.EN_COURS)).thenReturn(List.of());
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.startTask(1L, 7L);

        assertEquals(Status.EN_COURS, result.getStatus());
        verify(mailService).sendMail(eq(u), anyString(), anyString());
    }

    @Test
    void testFinishTask() {
        User manager = new User("manager");
        manager.setId(99L);

        User u = new User("hiba");
        u.setId(7L);
        u.setManager(manager);

        Task t = new Task("T", "D", u);
        t.setId(1L);
        t.setStatus(Status.EN_COURS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.finishTask(1L, 7L);

        assertEquals(Status.FINI, result.getStatus());
        verify(mailService).sendMail(eq(u), anyString(), anyString());
        verify(mailService).sendMail(eq(manager), anyString(), anyString());
    }
}
