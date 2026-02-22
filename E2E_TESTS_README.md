# E2E Tests for Todo Reactive API

## Overview

This directory contains comprehensive end-to-end (E2E) tests for the Todo Reactive API. These tests validate the complete flow of the application including authentication, authorization, and all CRUD operations.

## Test File Location

- **Main Test Class**: `src/test/java/com/example/todowebapp/api/TodoControllerE2ETest.java`
- **Test Configuration**: `src/test/resources/application-test.properties`
- **Database Schema**: `src/test/resources/schema.sql`

## Test Coverage

### 1. **GET /api/todos - Retrieve Todos** (`GetTodosTests`)
- ✅ Retrieve empty list when user has no todos
- ✅ Retrieve all todos for authenticated user
- ✅ Return 401 when authorization header is missing
- ✅ Return 401 when token is invalid

### 2. **POST /api/todo/create - Create Todo** (`CreateTodoTests`)
- ✅ Create a new todo successfully
- ✅ Create todo with completion date when marked as completed
- ✅ Fail validation when description is missing
- ✅ Fail validation when due date is missing
- ✅ Return 401 when authorization header is missing
- ✅ Return 401 when token is invalid

### 3. **PUT /api/todo/update - Update Todo** (`UpdateTodoTests`)
- ✅ Update todo successfully
- ✅ Mark todo as complete with completion date
- ✅ Fail when todo id is missing
- ✅ Fail when todo does not exist
- ✅ Fail when user tries to update another user's todo
- ✅ Return 401 when authorization header is missing

### 4. **DELETE /api/todo/delete - Delete Todos** (`DeleteTodosTests`)
- ✅ Delete single todo successfully
- ✅ Delete multiple todos successfully
- ✅ Fail validation when ids set is empty
- ✅ Fail when ids is null
- ✅ Not delete todos that don't belong to user (security check)
- ✅ Return 401 when authorization header is missing

## Test Architecture

### Nested Test Classes

The test class uses JUnit 5's `@Nested` annotation to organize tests into logical groups:

```
TodoControllerE2ETest
├── GetTodosTests
├── CreateTodoTests
├── UpdateTodoTests
└── DeleteTodosTests
```

This structure improves readability and organization of test methods.

### Setup & Teardown

**`setUp()` method** - Runs before each test:
1. Cleans up data from previous tests
2. Creates test role (ROLE_BASIC_USER)
3. Creates test user with known credentials
4. Authenticates user and retrieves JWT token

**`setUpTodos()` method** - Available in update/delete test classes:
- Creates test todos with specific characteristics for each test class

## Test Configuration

### H2 In-Memory Database

The tests use H2 in-memory database for fast, isolated test execution:

```properties
spring.r2dbc.url=r2dbc:h2:mem:test;...
spring.flyway.enabled=false
spring.sql.init.mode=always
```

### Schema Initialization

`schema.sql` automatically initializes:
- Database tables (roles, users, todos, privileges)
- Default roles (ROLE_BASIC_USER, ROLE_STANDARD_USER, etc.)
- Default privileges (VIEW_TODOS, CREATE_TODOS, etc.)

## Running the Tests

### Run all E2E tests
```bash
./mvnw test -Dtest=TodoControllerE2ETest
```

### Run specific test class
```bash
./mvnw test -Dtest=TodoControllerE2ETest$GetTodosTests
./mvnw test -Dtest=TodoControllerE2ETest$CreateTodoTests
./mvnw test -Dtest=TodoControllerE2ETest$UpdateTodoTests
./mvnw test -Dtest=TodoControllerE2ETest$DeleteTodosTests
```

### Run with verbose output
```bash
./mvnw test -Dtest=TodoControllerE2ETest -X
```

## Test Data

### Test User
- **Email**: `testuser@example.com`
- **Password**: `testpass123`
- **Role**: ROLE_BASIC_USER

### Test Todos
Various todos are created in each test's `@BeforeEach` setup method with different characteristics:
- Todos with different due dates
- Completed and incomplete todos
- Todos with and without completion dates

## Key Testing Patterns

### 1. **Authentication Testing**
```java
LoginData loginData = LoginData.builder()
    .email(TEST_EMAIL)
    .password(TEST_PASSWORD)
    .build();

// Use returned JWT token in subsequent requests
webTestClient.get()
    .uri(API_BASE + "/todos")
    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
```

### 2. **Request/Response Testing**
```java
webTestClient.post()
    .uri(API_BASE + "/todo/create")
    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(newTodo)
    .exchange()
    .expectStatus().isOk()
    .expectBody(TodoDTO.class)
    .consumeWith(response -> {
        TodoDTO result = response.getResponseBody();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    });
```

### 3. **Data Verification**
```java
// Verify changes persisted to database
var remainingTodos = todoRepository.findAllByUserId(userId).collectList().block();
assertThat(remainingTodos).hasSize(expectedSize);
```

### 4. **Security Testing**
```java
// Test unauthorized access
webTestClient.get()
    .uri(API_BASE + "/todos")
    .exchange()
    .expectStatus().isUnauthorized();

// Test cross-user access prevention
webTestClient.put()
    .uri(API_BASE + "/todo/update")
    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(updateAnotherUsersTodo)
    .exchange()
    .expectStatus().isBadRequest();
```

## Technologies & Dependencies

- **Spring Boot 3.3.4** - Reactive framework
- **Spring WebFlux** - Reactive web framework
- **Spring Security** - Authentication & authorization
- **JWT (JSON Web Tokens)** - Token-based authentication
- **JUnit 5** - Testing framework
- **AssertJ** - Assertion library
- **H2 Database** - In-memory database for tests
- **WebTestClient** - Reactive HTTP client for testing

## Dependencies in pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Notes

### Reactive Testing
- Uses `block()` for blocking operations in test setup (acceptable for tests)
- WebTestClient handles reactive streams transparently
- Each test is isolated and doesn't affect others

### Transaction Management
- Tests use `@Transactional` annotations where needed
- Data is automatically cleaned up before each test

### Performance
- H2 in-memory database is very fast
- Full test suite runs in seconds
- Suitable for CI/CD pipelines

## Future Enhancements

- [ ] Add performance benchmarking tests
- [ ] Add concurrency/race condition tests
- [ ] Add batch operation tests
- [ ] Add error scenario coverage
- [ ] Add pagination and filtering tests
- [ ] Add advanced query tests (sorting, searching)

## Troubleshooting

### Tests fail with "ApplicationContext failure"
- Check that schema.sql is in `src/test/resources`
- Verify `application-test.properties` has correct profile settings
- Ensure all required dependencies are in pom.xml

### Authentication fails
- Verify JWT secret key is set in `application-test.properties`
- Check that login endpoint is working correctly
- Ensure user is created before authenticating

### Database connection issues
- Verify H2 URL in test properties
- Check that pool settings are appropriate
- Ensure schema initialization is enabled

## Contributing

When adding new API endpoints:
1. Create corresponding test methods in the appropriate nested class
2. Test both happy path and error scenarios
3. Include authentication/authorization checks
4. Verify data persistence where applicable
5. Add cross-user security tests for user-specific operations


