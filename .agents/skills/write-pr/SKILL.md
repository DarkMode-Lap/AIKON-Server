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

`type :: 설명` — same format as commit messages, reflecting the overall change.

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

- Title: concise, reflects the most important change
- Body: Korean, focus on "why" not just "what"
- 체크리스트는 항목 그대로 유지 — 체크 여부는 작성자가 직접 판단
- Do NOT include file names in the body unless critical context
