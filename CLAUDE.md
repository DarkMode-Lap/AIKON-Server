## Project Overview

AIKON is a Kotlin/Spring Boot REST API server built by team DarkMode-Lap.

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Format: `./gradlew ktlintFormat`
- Run: `./gradlew bootRun`

## Tech Stack

Kotlin, Spring Boot 4.0.6, Spring Security, Spring Data JPA, PostgreSQL

## Coding Rules

- Controller → Service → Repository pattern
- Use constructor injection
- Prefer `val` over `var`
- Do NOT add excessive comments — only where logic is not self-evident

Detailed rules are split into `.claude/rules/`:
- `kotlin-style.md` — `val/var`, constructor injection, null safety
- `api-conventions.md` — `@RequestParam` vs `@ModelAttribute`, DTO naming, `@Transactional` placement
- `commit-conventions.md` — commit type/scope rules
- `exception.md` — exception usage and message format
- `logging.md` — English-only, SLF4J `{}` placeholders
- `entity.md` — JPA entity rules, column conventions, relationship fetch strategy
- `test-conventions.md` — test class structure, naming, Given-When-Then, scope

## Branch Strategy

Git Flow — feature branches from `develop`, merge back to `develop`.
Branch naming: `type/kebab-case-description`

## Context Compaction Rules

Priority order when compressing conversation history:
1. Project Overview
2. Coding Rules
3. Tech Stack
4. Reduce: Commands, Key Paths

## Key Paths

- Entry Point: `src/main/kotlin/team/darkmoderap/aikon/AikonApplication.kt`
- Config: `src/main/resources/application.yml`
