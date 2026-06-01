---
name: test
description: Write JUnit 5 tests for service or controller classes following project conventions. Given-When-Then structure with Spring Boot Test and Mockito/MockK.
allowed-tools: Bash, Read, Edit, Write
---

## Step 1 — Identify What to Test

Read the target class:
1. Identify all public methods
2. Note dependencies (repositories, other services)
3. Identify edge cases and error paths

## Step 2 — Check Existing Tests

```bash
find src/test -name "*.kt" | grep -i "$(basename $TARGET .kt)"
```

Read any existing tests to follow established patterns.

## Step 3 — Write Tests

### Test Class Structure

```kotlin
@ExtendWith(MockitoExtension::class)
class UserServiceImplTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {

        @Test
        @DisplayName("존재하는 사용자를 정상 조회한다")
        fun `returns user when found`() {
            // Given
            val userId = 1L
            val user = User(id = userId, name = "테스트")
            given(userRepository.findById(userId)).willReturn(Optional.of(user))

            // When
            val result = userService.execute(userId)

            // Then
            assertThat(result.id).isEqualTo(userId)
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 예외를 던진다")
        fun `throws exception when not found`() {
            // Given
            given(userRepository.findById(any())).willReturn(Optional.empty())

            // When & Then
            assertThrows<CustomException> { userService.execute(999L) }
        }
    }
}
```

## Conventions

- Test class name: `{TargetClass}Test`
- `@DisplayName`: Korean, describes the method under test
- Structure: Given / When / Then comments
- Mock with `@Mock` + `@InjectMocks` (Mockito) or MockK
- Place test files mirroring the source structure under `src/test/`
