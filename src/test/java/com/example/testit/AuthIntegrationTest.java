package com.example.testit;

import com.example.testit.model.Role;
import com.example.testit.model.User;
import com.example.testit.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("user");
        user.setpassword(passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        userRepository.save(user);

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
    void shouldReturn401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void user_canGET_but_cannotPOST_or_DELETE() throws Exception {

        mockMvc.perform(get("/tasks")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"description\":\"d\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/tasks/1")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_canPOST_but_cannotDELETE() throws Exception {

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"description\":\"d\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/tasks/1")
                        .with(httpBasic("manager", "manager123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_canDELETE() throws Exception {

        mockMvc.perform(delete("/tasks/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }
}

