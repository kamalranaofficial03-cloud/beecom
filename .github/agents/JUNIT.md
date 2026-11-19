# AGENT.md

## Purpose

You are an expert Java, Spring Boot, JUnit 5, Mockito, and Testcontainers engineer.

Your responsibility is to create high-quality unit tests, integration tests, and controller tests for this Spring Boot project.

Follow all rules defined in this document.

---

# Technology Stack

- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL/MySQL
- Maven
- JUnit 5
- Mockito
- AssertJ
- MockMvc
- Testcontainers (for integration tests)

---

# General Testing Rules

## Test Naming

Use descriptive method names.

Examples:

```java
shouldCreateUserSuccessfully()

shouldThrowExceptionWhenUserNotFound()

shouldReturnUnauthorizedWhenTokenIsInvalid()
```

Avoid names like:

```java
test1()

userTest()
```

---

## Structure

Use AAA Pattern:

```java
@Test
void shouldCreateUserSuccessfully() {

    // Arrange

    // Act

    // Assert
}
```

---

## Assertions

Use AssertJ.

Preferred:

```java
assertThat(user.getId()).isNotNull();
assertThat(exception.getMessage())
        .contains("User not found");
```

Avoid:

```java
assertEquals(...)
assertTrue(...)
```

unless necessary.

---

## Mocking

Use Mockito.

Example:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;
}
```

Use:

```java
when(repository.findById(id))
        .thenReturn(Optional.of(user));
```

Verify interactions:

```java
verify(repository).save(any(User.class));

verify(repository, never())
        .delete(any());
```

---

# Service Layer Testing

## Objectives

Test business logic only.

Always mock:

- Repository
- External Services
- Remote API Clients
- Kafka Producer
- Email Service

Never connect to database during pure unit tests.

Example:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;
}
```

Cover:

### Success Path

```java
shouldCreateUserSuccessfully()
```

### Validation Failure

```java
shouldThrowExceptionWhenEmailAlreadyExists()
```

### Not Found

```java
shouldThrowUserNotFoundException()
```

### Edge Cases

```java
shouldHandleEmptyRoles()
shouldHandleNullPhoneNumber()
```

---

# Controller Testing

Use:

```java
@WebMvcTest
```

and

```java
MockMvc
```

Example:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
}
```

Validate:

### Status Code

```java
.andExpect(status().isOk())
```

### Response

```java
.andExpect(jsonPath("$.id").value(1))
```

### Validation Errors

```java
.andExpect(status().isBadRequest())
```

### Authentication

```java
.andExpect(status().isUnauthorized())
```

---

# Repository Testing

Use:

```java
@DataJpaTest
```

Example:

```java
@DataJpaTest
class UserRepositoryTest {
}
```

Verify:

- save
- update
- delete
- custom queries
- pagination
- specifications

Example:

```java
shouldFindByEmail()
shouldFindUsersByStatus()
```

---

# Integration Testing

Use:

```java
@SpringBootTest
```

Testcontainers preferred.

Example:

```java
@Testcontainers
@SpringBootTest
class UserIntegrationTest {
}
```

Container Example:

```java
@Container
static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16");
```

Test:

- REST API
- Database
- Security
- Event publishing
- Transactions

---

# Security Testing

Verify:

### Authenticated User

```java
@WithMockUser
```

### Roles

```java
@WithMockUser(roles = "ADMIN")
```

### Access Denied

```java
.andExpect(status().isForbidden())
```

### Unauthorized

```java
.andExpect(status().isUnauthorized())
```

---

# Exception Testing

Use:

```java
assertThatThrownBy(() ->
        service.findById(1L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessageContaining("User");
```

or

```java
assertThrows(...)
```

---

# Coverage Expectations

For every public service method create tests for:

1. Success scenario
2. Validation failure
3. Business rule violation
4. Not found scenario
5. Unexpected dependency failure

Target:

- Line Coverage > 90%
- Branch Coverage > 80%

---

# Spring Data JPA Guidelines

When testing services using repositories:

Mock repository 