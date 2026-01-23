package com.example.testit.service;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Status;
import com.example.testit.model.Task;
import com.example.testit.model.User;
import com.example.testit.repository.TaskRepository;
import com.example.testit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class TaskServiceIT {

    @Autowired TaskService taskService;
    @Autowired UserRepository userRepository;
    @Autowired TaskRepository taskRepository;

    @MockBean MailService mailService;

    @Test
    void testCreateTaskIntegration() {
        User requester = userRepository.save(new User("requester"));
        User assigned = userRepository.save(new User("assigned"));

        Task created = taskService.createTask("T", "D", requester.getId(), assigned.getId());

        assertNotNull(created.getId());
        assertEquals(Status.OUVERT, created.getStatus());
        assertEquals(assigned.getId(), created.getAssignedUser().getId());
        assertEquals(requester.getId(), created.getRequester().getId());
    }

    @Test
    void testStartTaskIntegration() {
        User assigned = userRepository.save(new User("assigned"));
        User requester = userRepository.save(new User("requester"));

        Task t = new Task("T", "D", assigned);
        t.setRequester(requester);
        t.setStatus(Status.OUVERT);
        t = taskRepository.save(t);

        Task started = taskService.startTask(t.getId(), assigned.getId());

        assertEquals(Status.EN_COURS, started.getStatus());
        verify(mailService).sendMail(eq(assigned), anyString(), anyString());
    }
}
