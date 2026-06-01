# Commit & PR Conventions

## Commit Message Format

`type :: description`

- **Description**: Korean, no period

## Commit Types

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

## Examples

```
add :: JWT 토큰 발급 구현
fix :: 사용자 조회 쿼리 수정
delete :: 불필요한 테스트 파일 제거
update :: CI 빌드 설정 변경
init :: 프로젝트 초기 설정
```

## Branch Naming

Format: `prefix/kebab-case-description`
Allowed prefixes: `feat/`, `fix/`, `update/`, `add/`, `delete/`, `docs/`, `test/`, `init/`.
Branch prefixes are independent from commit message types. Use `feat/` for feature work even when the commit type is `add`.

```
feat/jwt-auth
fix/user-query-bug
update/optimize-post-query
```
