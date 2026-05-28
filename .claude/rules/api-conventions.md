# API Conventions

## Query Parameter Binding

- **1-2 simple parameters**: Use `@RequestParam`
- **3+ parameters or validation required**: Use `@ModelAttribute` + DTO

```kotlin
// 1-2 parameters → @RequestParam
@GetMapping("/users")
fun getUser(@RequestParam id: Long): UserResDto

// 3+ parameters → @ModelAttribute + DTO
@GetMapping("/users/search")
fun searchUsers(@Valid @ModelAttribute queryReq: SearchUserReqDto): UserListResDto
```

## DTO Variable Naming

- **`@RequestBody`** (Create/Update): `reqDto`
- **`@ModelAttribute`** (Query): `queryReq`

## Controller → Service Passing

Pass DTO objects as-is. `@PathVariable` values can be passed individually.

```kotlin
@PostMapping
fun createUser(@Valid @RequestBody reqDto: CreateUserReqDto): UserResDto =
    createUserService.execute(reqDto)

@PutMapping("/{id}")
fun updateUser(@PathVariable id: Long, @Valid @RequestBody reqDto: UpdateUserReqDto): UserResDto =
    updateUserService.execute(id, reqDto)
```

## Transaction Rules

- `@Transactional` must be at **method level** — never class level
- Read operations: `@Transactional(readOnly = true)`
- Write operations: `@Transactional`

```kotlin
// CORRECT
@Transactional(readOnly = true)
override fun execute(queryReq: SearchUserReqDto): UserListResDto { ... }

// WRONG
@Service
@Transactional  // class-level is forbidden
class UserServiceImpl { ... }
```
