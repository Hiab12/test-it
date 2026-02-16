package com.example.testit;

import com.example.testit.model.Role;
import com.example.testit.model.Task;
import com.example.testit.model.User;
import com.example.testit.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long userId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("user");
        user.setpassword(passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        userRepository.save(user);
        userId = user.getId();

        User manager = new User();
        manager.setUsername("manager");
        manager.setpassword(passwordEncoder.encode("manager123"));
        manager.setRole(Role.MANAGER);
        userRepository.save(manager);

        User admin = new User();
        admin.setUsername("admin");
        admin.setpassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }


    @Test
    void getAllTasks_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllTasks_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/tasks")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void createTask_shouldReturn401_whenNotAuthenticated() throws Exception {
        String taskJson = """
            {
              "title": "Test Task",
              "description": "Test Description"
            }
            """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_shouldCreateTask_whenAuthenticated() throws Exception {
        String taskJson = """
            {
              "title": "Test Task",
              "description": "Test Description"
            }
            """;

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("OUVERT"));
    }

    @Test
    void user_canReadTasks_butCannotCreate() throws Exception {
        mockMvc.perform(get("/tasks").with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        String taskJson = """
        {"title":"T1","description":"hello"}
        """;

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_canCreateAndAssign() throws Exception {
        String createJson = """
        {"title":"T1","description":"hello"}
        """;

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T1"));

        String assignJson = """
{"title":"T2","description":"assign","userId": %d}
""".formatted(userId);


        mockMvc.perform(post("/tasks")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("T2"))
                .andExpect(jsonPath("$.assignedUser.username").value("user"));
    }

    @Test
    void admin_canDelete_butManager_cannotDelete() throws Exception {

        String createJson = """
        {"title":"T1","description":"hello"}
        """;

        String response = mockMvc.perform(post("/tasks")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        String idStr = response.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");
        Long id = Long.parseLong(idStr);


        mockMvc.perform(delete("/tasks/{id}", id)
                        .with(httpBasic("manager", "manager123")))
                .andExpect(status().isForbidden());


        mockMvc.perform(delete("/tasks/{id}", id)
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }
    @Test
    void user_cannotUpdateTask() throws Exception {
        Long taskId = createTaskAsManager("Tupdate");

        String updateJson = """
            {"id": %d, "title":"NEW", "description":"changed"}
            """.formatted(taskId);

        mockMvc.perform(put("/tasks/{id}", taskId)
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }

    private Long createTaskAsManager(String title) throws Exception {

        String json = """
        {
            "title": "%s",
            "description": "desc"
        }
        """.formatted(title);

        String response = mockMvc.perform(post("/tasks")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Task created = mapper.readValue(response, Task.class);

        return created.getId();
    }

}