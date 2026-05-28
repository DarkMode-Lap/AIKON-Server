---
name: write-pr
description: Write a GitHub Pull Request title and body based on the commits and changes on the current branch compared to develop. Follows the project PR template at .github/PULL_REQUEST_TEMPLATE.md.
allowed-tools: Bash, Read
---

## Step 1 — Collect Branch Info

```bash
git log develop...HEAD --oneline
git diff develop...HEAD --stat
```

## Step 2 — Understand Changes

Read the key changed files to understand what was implemented.

## Step 3 — Write PR

### Title Format

짧고 명확한 한국어 설명 — `type ::` 접두사 없이 작성한다.

예시: `Claude Code 및 Codex AI 에이전트 개발 환경 설정 추가`

### Body Template

Follow `.github/PULL_REQUEST_TEMPLATE.md` exactly:

```markdown
## ✨ 작업 내용
[이번 PR에서 어떤 작업을 했는지 2-3줄로 요약]

---

## 🔍 리뷰 시 참고사항
- [리뷰어가 알면 좋은 변경 이유, 배경, 고려했던 점]

---

## ✅ 체크리스트
- [ ] 문서(README, `.env.example` 등) 변경이 필요한 경우 작성 또는 수정했나요?
- [ ] 작업한 코드가 정상적으로 동작하는 것을 직접 확인했나요?
- [ ] 필요한 경우 테스트 코드를 작성하거나 수정했나요?
- [ ] Merge 대상 브랜치를 올바르게 설정했나요?
- [ ] PR에 관련 없는 작업이 포함되지 않았나요?
- [ ] 적절한 라벨과 리뷰어를 설정했나요?

---

## 📎 관련 이슈(선택)
- Close #
```

## Guidelines

- Title: concise, reflects the most important change — NO `type ::` prefix
- Body: Korean, focus on "why" not just "what"
- Do NOT include file names in the body unless critical context

### Checklist Rules

변경 내용을 분석해 각 항목을 `[x]` 또는 `[ ]`로 판단한다:

| 항목 | 체크 조건 |
|------|-----------|
| 문서 변경이 필요한 경우 작성 또는 수정했나요? | README, `.env.example`, `CLAUDE.md` 등 문서가 변경됐거나, 변경 내용이 문서 업데이트를 필요로 하지 않으면 `[x]` |
| 작업한 코드가 정상적으로 동작하는 것을 직접 확인했나요? | 동작 확인이 불가능한 설정/문서 전용 PR이 아닌 이상 판단 보류 → `[ ]` |
| 필요한 경우 테스트 코드를 작성하거나 수정했나요? | 테스트 파일이 변경됐거나, 비즈니스 로직 변경이 없어 테스트가 불필요하면 `[x]` |
| Merge 대상 브랜치를 올바르게 설정했나요? | `--base develop`으로 생성하므로 항상 `[x]` |
| PR에 관련 없는 작업이 포함되지 않았나요? | 커밋 내용이 단일 목적이면 `[x]` |
| 적절한 라벨과 리뷰어를 설정했나요? | 자동 설정 불가 → 항상 `[ ]` |

### Related Issue Rules

- `gh issue list --state open` 으로 열린 이슈를 조회한다.
- 브랜치명·커밋 메시지와 관련된 이슈가 있으면 `- Close #<번호>` 로 기재한다.
- 관련 이슈가 없으면 해당 섹션을 완전히 제거한다.

## Step 4 — Create PR

1. 열린 이슈 조회:
```bash
gh issue list --state open
```

2. 브랜치 푸시 및 PR 생성 (`--body-file` 사용):
```bash
git push -u origin HEAD
# PR body를 /tmp/pr-body.md 에 Write 도구로 저장한 뒤:
gh pr create --base develop --title "<title>" --body-file /tmp/pr-body.md
```

After creation, print the PR URL.
