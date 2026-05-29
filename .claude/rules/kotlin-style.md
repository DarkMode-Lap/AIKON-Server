# Kotlin Style Rules

## Immutability

Prefer `val` over `var`. Use `var` only when reassignment is strictly required.

```kotlin
// CORRECT
val user = userRepository.findById(id).orElseThrow()

// WRONG
var user = userRepository.findById(id).orElseThrow()
```

## Dependency Injection

Always use constructor injection — never field injection:

```kotlin
// CORRECT
@Service
class UserService(
    private val userRepository: UserRepository
)

// WRONG
@Service
class UserService {
    @Autowired
    lateinit var userRepository: UserRepository
}
```

## Comments

Do NOT add excessive comments. Only add comments where the logic is not self-evident.

## Null Safety

Use Kotlin null-safety features instead of unchecked access:

```kotlin
// CORRECT
val name = user?.name ?: "Unknown"

// WRONG
val name = user!!.name
```
