# JPA Entity Rules

## Base Entity

모든 엔티티는 공통 필드(createdAt, updatedAt 등)를 가진 BaseEntity를 상속한다.

```kotlin
// CORRECT
@Entity
class User(
    val name: String
) : BaseEntity()

// WRONG — BaseEntity 미상속
@Entity
class User(
    val name: String
)
```

## Class Declaration

- `data class` 사용 금지 — `equals`/`hashCode` 오작동 유발
- 생성자에 필드 선언, 기본값 설정으로 JPA 프록시 호환

```kotlin
// CORRECT
@Entity
class User(
    @Column(nullable = false)
    val name: String,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
```

## Column Rules

- `@Column(nullable = false)` — NOT NULL 컬럼에 명시
- 컬럼명은 snake_case: `@Column(name = "user_name")`
- 문자열 길이 명시: `@Column(length = 100)`

## ID Strategy

- `@GeneratedValue(strategy = GenerationType.IDENTITY)` 사용
- ID 필드는 기본값 `0`으로 선언하여 불필요한 null 처리 방지

```kotlin
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
val id: Long = 0
```

## Relationship

- 연관관계 주인 쪽에 `@JoinColumn` 명시
- `FetchType.LAZY` 기본 사용 — EAGER 금지
- N+1 방지: Fetch Join 또는 `@EntityGraph` 사용

```kotlin
// CORRECT
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
val user: User
```
