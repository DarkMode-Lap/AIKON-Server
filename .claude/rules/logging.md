# Logging Rules

## Language

English only — verb-led sentences.

## Placeholder

SLF4J `{}` placeholder only:
- No Kotlin string interpolation (`$variable`)
- No colon separators before `{}`

```kotlin
// CORRECT
logger.info("Fetched {} users from database", count)
logger.error("Failed to process request for user {}", userId)

// WRONG — Korean
logger.error("에러 발생: $message")

// WRONG — string interpolation
logger.info("Processing user $userId")

// WRONG — colon before placeholder
logger.info("User count: {}", count)
```

## Logger Declaration

```kotlin
private val logger = LoggerFactory.getLogger(javaClass)
```
