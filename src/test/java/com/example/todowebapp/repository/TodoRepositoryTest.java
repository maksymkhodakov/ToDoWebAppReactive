package com.example.todowebapp.repository;

import com.example.todowebapp.domain.entity.Role;
import com.example.todowebapp.domain.entity.Todo;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TodoRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("tododb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Test
    @Transactional
    void testFindAllByUserId() {
        // 1. Створюємо та зберігаємо роль
        Role role = Role.builder()
                .userRole(UserRole.ROLE_BASIC_USER)
                .build();
        Role savedRole = roleRepository.save(role);

        // 2. Створюємо та зберігаємо користувача із збереженою роллю
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .name("Test")
                .lastName("User")
                .role(savedRole)
                .system(false)
                .build();
        User savedUser = userRepository.save(user);

        // 3. Створюємо Todos, встановлюємо зв'язок із збереженим користувачем та зберігаємо його
        Todo todo1 = Todo.builder()
                .description("todo1")
                .dueDate(LocalDate.now())
                .checkMark(false)
                .build();
        todo1.setUser(savedUser);
        todoRepository.save(todo1);

        // 4. Виконуємо запит до репозиторію для отримання Todos за userId
        List<Todo> todosForUser = todoRepository.findAllByUserId(savedUser.getId());
        assertThat(todosForUser)
                .hasSize(1)
                .extracting(Todo::getDescription)
                .containsExactly("todo1");
    }

    @Test
    @Transactional
    void testFindAllByUserIdAndCreateDateAfter() {
        // 1. Створюємо та зберігаємо роль
        Role role = Role.builder()
                .userRole(UserRole.ROLE_BASIC_USER)
                .build();
        Role savedRole = roleRepository.save(role);

        // 2. Створюємо та зберігаємо користувача із збереженою роллю
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .name("Test")
                .lastName("User")
                .role(savedRole)
                .system(false)
                .build();
        User savedUser = userRepository.save(user);

        // 3. Створюємо та зберігаємо Todos (createDate встановиться автоматично)
        Todo todo = Todo.builder()
                .description("Recent todo")
                .dueDate(LocalDate.now())
                .checkMark(false)
                .build();
        todo.setUser(savedUser);
        todoRepository.save(todo);

        // 4. Виконуємо запит: беремо всі Todos для savedUser,
        LocalDateTime pastThreshold = LocalDateTime.now().minusMinutes(1);
        Page<Todo> recentPage = todoRepository.findAllByUserIdAndCreateDateAfter(
                savedUser.getId(),
                pastThreshold,
                PageRequest.of(0, 10)
        );
        List<Todo> recentTodos = recentPage.getContent();

        // збережений todos має createDate після порогового часу
        assertThat(recentTodos)
                .isNotEmpty()
                .extracting(Todo::getDescription)
                .contains("Recent todo");

        // 5. Перевіримо негативний сценарій:
        LocalDateTime futureThreshold = LocalDateTime.now().plusMinutes(1);
        Page<Todo> futurePage = todoRepository.findAllByUserIdAndCreateDateAfter(
                savedUser.getId(),
                futureThreshold,
                PageRequest.of(0, 10)
        );
        assertThat(futurePage.getContent()).isEmpty();
    }
}
