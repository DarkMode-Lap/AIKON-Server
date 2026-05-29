# Test Conventions

## Scope

- 서비스 로직은 반드시 단위 테스트 작성
- Controller는 통합 테스트(`@SpringBootTest`) 또는 슬라이스 테스트(`@WebMvcTest`)
- Repository는 슬라이스 테스트(`@DataJpaTest`)

## Class Naming

`{TargetClass}Test`

```kotlin
// CORRECT
class UserServiceImplTest
class UserControllerTest

// WRONG
class UserTest
class TestUser
```

## Structure

`@ExtendWith(MockitoExtension::class)` + `@Nested` + `@DisplayName` (한국어)

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

            // When

            // Then
        }
    }
}
```

## DisplayName Rules

- `@DisplayName`: 한국어, 자연어 문장
- `describe` 클래스: `"메서드명 메서드는"`
- `it` 케이스: 결과 중심으로 서술

## Given-When-Then

모든 테스트 메서드에 `// Given`, `// When`, `// Then` 주석 유지

## What to Test

- 정상 경로 (happy path)
- 예외 경로 (존재하지 않는 리소스, 권한 없음 등)
- 경계값 (빈 리스트, null 등)

## What NOT to Test

- 단순 getter/setter
- 프레임워크 동작 (Spring DI, JPA 기본 CRUD)
