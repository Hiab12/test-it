package com.example.testit;

import com.example.testit.model.User;
import com.example.testit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldPersistUser() {
        User user = new User("testuser");
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_shouldReturnUser_whenExists() {
        User user = userRepository.save(new User("findme"));

        User found = userRepository.findByUsername("findme");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("findme");
    }

    @Test
    void findByUsername_shouldReturnNull_whenNotExists() {
        User found = userRepository.findByUsername("nonexistent");

        assertThat(found).isNull();
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User saved = userRepository.save(new User("findbyid"));

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("findbyid");
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        User saved = userRepository.save(new User("exists"));

        boolean exists = userRepository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        boolean exists = userRepository.existsById(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldPersistUserWithManager() {
        User manager = userRepository.save(new User("manager"));

        User subordinate = new User("sub");
        subordinate.setManager(manager);
        User saved = userRepository.save(subordinate);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getManager().getUsername()).isEqualTo("manager");
    }
    @Test
    void save_shouldPersistUserWithoutManager() {
        User manager = userRepository.save(new User("manager"));


        assertThat(manager.getId()).isNotNull();
    }

    @Test
    void selfReference_shouldAllowUserAsOwnManager() {
        User user = new User("selfmanager");
        user.setManager(user);  // Auto-référence
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getManager().getUsername()).isEqualTo("selfmanager");
    }

}
