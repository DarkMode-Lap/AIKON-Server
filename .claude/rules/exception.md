# Exception Handling Rules

## Custom Exception

프로젝트에서 정의한 커스텀 예외 클래스를 직접 사용한다. 서브클래싱하지 않는다.

## Message Format

- 한국어 합쇼체 + 마침표
- 동적 데이터(ID, 이름, 변수값) 포함 금지

```kotlin
// CORRECT
throw CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

// WRONG — dynamic data in message
throw CustomException("사용자 ID: $id 없음", HttpStatus.NOT_FOUND)

// WRONG — no period
throw CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND)
```

## Exception Handler

전역 예외 처리는 `@RestControllerAdvice`를 사용한다.
