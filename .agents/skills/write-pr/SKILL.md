---
name: write-pr
description: Write a GitHub Pull Request title and body based on the commits and changes on the current branch compared to develop. Follows the project PR template at .github/PULL_REQUEST_TEMPLATE.md.
allowed-tools: Bash, Read
---

This is the official PR writing skill for this project. When PR creation is requested, follow this skill before taking action.

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

- Title: concise, reflects the most important change
- Body: Korean, focus on "why" not just "what"
- 체크리스트는 항목 그대로 유지 — 체크 여부는 작성자가 직접 판단
- Do NOT include file names in the body unless critical context

## Step 4 — Collect Repo Meta

```bash
gh api user --jq '.login'
gh label list
```

### Reviewer Rules

- Always request reviewers when creating a PR.
- Fixed reviewer candidates: `cfcromn`, `jyx-07`
- Exclude the current GitHub user from reviewer candidates.
- If no reviewer remains after exclusion, omit the reviewer flag.

### Label Rules

- Use 1–3 labels only when matching labels exist in the repository.
- If no matching label exists, omit the label flag.

## Step 5 — Create PR

Push the current branch and create the PR:

```bash
git push -u origin HEAD
gh pr create --base develop \
  --title "<title>" \
  --body "<body>" \
  --reviewer "<reviewer1>,<reviewer2>" \
  --label "<label1>,<label2>"
```

- Omit `--reviewer` when there is no reviewer to request.
- Omit `--label` when there is no label to apply.
- Do not assign the PR author automatically.
