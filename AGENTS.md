**한국어로 응답하고 작업해주세요 (Please respond and work in Korean).**

## Project Overview

AIKON is a Kotlin/Spring Boot REST API server built by team DarkMode-Lap.

## Tech Stack

- **Backend**: Kotlin, Spring Boot 4.0.6, Spring Security, Spring Data JPA
- **Database**: PostgreSQL
- **Testing**: JUnit 5 + Spring Boot Test

## Project Structure

```
aikon-server/
└── src/main/kotlin/team/darkmoderap/aikon/
    └── AikonApplication.kt
```

Each domain follows: `controller/`, `service/`, `repository/`, `entity/`, `dto/`

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew bootRun`

## Agent Skills

- The official project skill directory is `.agents/skills`.
- For commit requests, read and follow `.agents/skills/commit/SKILL.md` before running any commit command.
- For PR creation requests, read and follow `.agents/skills/write-pr/SKILL.md`.
- For PR review feedback requests, read and follow `.agents/skills/review-pr/SKILL.md`.
- For test creation requests, read and follow `.agents/skills/test/SKILL.md`.
- If a global skill under `~/.codex/skills` has the same name, prefer this project's `.agents/skills` version.

## Coding Conventions

### Kotlin Style

- Prefer `val` over `var`. Use `var` only when reassignment is strictly required.
- Always use constructor injection — never `@Autowired` field injection.
- Use Kotlin null-safety features (`?.`, `?:`) instead of `!!`.
- Do NOT add excessive comments — only where logic is not self-evident.

### API Conventions

- 1–2 query params: use `@RequestParam`; 3+ or with validation: use `@ModelAttribute` + DTO
- `@RequestBody` variable: `reqDto`; `@ModelAttribute` query: `queryReq`
- `@Transactional` must be at **method level only** — never class level
- Read operations: `@Transactional(readOnly = true)` / Write operations: `@Transactional`

### Entity Rules

- All entities extend `BaseEntity`
- No `data class` for entities
- `@Column(nullable = false)` for NOT NULL columns, snake_case column names
- `FetchType.LAZY` by default — no EAGER loading

### Logging

- English only — verb-led sentences
- SLF4J `{}` placeholder only — no Kotlin string interpolation, no colon separators
- Correct: `logger.info("Fetched {} items", count)`
- Wrong: `logger.error("에러: $message")` or `logger.error("Failed: {}", msg)`

### Exception Handling

- Use custom exception class directly — do NOT subclass it
- Message: Korean 합쇼체 + period, no dynamic data (IDs, names, variables)

### Test Conventions

- Service logic must have unit tests (`@ExtendWith(MockitoExtension::class)`)
- `@Nested` + `@DisplayName` (Korean), Given-When-Then structure
- Test class name: `{TargetClass}Test`

### Commit Conventions

Format: `type :: 설명`

| Type | Meaning |
| --- | --- |
| add | 새로운 코드나 파일을 추가하였을 때 |
| update | 기존의 코드를 수정했을 때 |
| fix | 버그를 수정했을 때 |
| delete | 삭제한 사항이 있을 때 |
| docs | 문서를 수정 |
| test | 테스트 관련 사항을 추가/수정하였을 때 |
| merge | 브랜치를 병합하였을 때 |
| init | 프로젝트 초기화 시 |

## Branch Strategy

Git Flow — feature branches from `develop`, merge back to `develop`.
Branch naming: `prefix/kebab-case-description`
Allowed branch prefixes: `feat/`, `fix/`, `update/`, `add/`, `delete/`, `docs/`, `test/`, `init/`.
Use `feat/` for feature work even when the commit type is `add`.

## Notes

- Always check `.gitignore` when suggesting file changes
