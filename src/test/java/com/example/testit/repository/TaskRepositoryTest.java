package com.example.testit.repository;

import com.example.testit.model.Status;
import com.example.testit.model.Task;
import com.example.testit.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TaskRepositoryTest {

    @Autowired TaskRepository taskRepository;
    @Autowired UserRepository userRepository;


    @Test
    void testFindByAssignedUserId() {
        User u = userRepository.save(new User("hiba"));

        taskRepository.save(new Task("A", "d", u));
        taskRepository.save(new Task("B", "d", u));

        List<Task> res = taskRepository.findByAssignedUserId(u.getId());

        assertEquals(2, res.size());
    }

    @Test
    void testFindByUserAndStatus() {
        User u = userRepository.save(new User("hiba"));

        Task t1 = new Task("A", "d", u);
        t1.setStatus(Status.OUVERT);
        taskRepository.save(t1);

        Task t2 = new Task("B", "d", u);
        t2.setStatus(Status.EN_COURS);
        taskRepository.save(t2);

        List<Task> res = taskRepository.findByUserAndStatus(u.getId(), Status.EN_COURS);

        assertEquals(1, res.size());
        assertEquals("B", res.get(0).getTitle());
    }
}
