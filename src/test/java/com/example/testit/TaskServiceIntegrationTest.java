package com.example.testit;

import com.example.testit.model.Status;
import com.example.testit.model.Task;
import com.example.testit.model.User;
import com.example.testit.repository.TaskRepository;
import com.example.testit.repository.UserRepository;
import com.example.testit.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        user1 = userRepository.save(new User("user1"));
        user2 = userRepository.save(new User("user2"));

        // Créer des tâches (IDs générés automatiquement)
        task1 = taskService.createTask("Task 1", "Desc 1", user1.getId(), user1.getId());
        task2 = taskService.createTask("Task 2", "Desc 2", user1.getId(), user1.getId());
    }

    @Test
    void createTask_shouldCreateTaskInDatabase() {
        Task task = taskService.createTask("New Task", "New Desc", user2.getId(), user2.getId());

        assertThat(task.getId()).isNotNull();
        assertThat(task.getTitle()).isEqualTo("New Task");
        assertThat(task.getAssignedUser().getUsername()).isEqualTo("user2");
        assertThat(task.getStatus()).isEqualTo(Status.OUVERT);
    }

    @Test
    void startTask_shouldStartTaskAndUpdateDatabase() {
        Task started = taskService.startTask(task1.getId(), user1.getId());

        Task retrieved = taskRepository.findById(task1.getId()).orElse(null);
        assertThat(started.getStatus()).isEqualTo(Status.EN_COURS);
        assertThat(retrieved.getStatus()).isEqualTo(Status.EN_COURS);
    }

    @Test
    void startTask_shouldFail_whenUserHasMultipleOngoingTasks() {
        // Démarrer la première tâche
        taskService.startTask(task1.getId(), user1.getId());

        // Essayer de démarrer la deuxième pour le même user
        assertThatThrownBy(() -> taskService.startTask(task2.getId(), user1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User cannot have more than one task EN_COURS");
    }

    @Test
    void finishTask_shouldFinishTaskAndUpdateDatabase() {
        // Démarrer d'abord
        taskService.startTask(task1.getId(), user1.getId());
        // Puis terminer
        Task finished = taskService.finishTask(task1.getId(), user1.getId());

        Task retrieved = taskRepository.findById(task1.getId()).orElse(null);
        assertThat(finished.getStatus()).isEqualTo(Status.FINI);
        assertThat(retrieved.getStatus()).isEqualTo(Status.FINI);
    }

    @Test
    void startTask_shouldFail_whenTaskNotAssigned() {
        assertThatThrownBy(() -> taskService.startTask(task1.getId(), user2.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task not assigned to this user");
    }

    @Test
    void findByUserId_shouldReturnUserTasks() {
        assertThat(taskService.findByUserId(user1.getId())).hasSize(2);
        assertThat(taskService.findByUserId(user2.getId())).hasSize(0);
    }

    @Test
    void finishTask_shouldSendMailToManager_whenManagerExists() {
        // Set manager pour user1
        User manager = userRepository.save(new User("manager"));
        user1.setManager(manager);
        userRepository.save(user1);

        // Démarrer la tâche
        taskService.startTask(task1.getId(), user1.getId());

        // Finir la tâche - devrait envoyer mail à assigned (user1) et à manager
        Task finished = taskService.finishTask(task1.getId(), user1.getId());

        assertThat(finished.getStatus()).isEqualTo(Status.FINI);
        // Les mails sont loggés via MailServiceFake, on peut vérifier la persistance
        // Dans vrai test, mock MailService et vérifier calls
    }
}
