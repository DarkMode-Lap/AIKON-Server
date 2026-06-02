---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions. Handles Git Flow automatically — detects develop branch and checks out a feature branch before committing.
allowed-tools: Bash
---

This is the official commit skill for this project. When a commit is requested, follow this skill before running any commit command.

## Step 0 — Branch Check (Required)

Check the current branch first:

```bash
git branch --show-current
```

**If current branch is `develop`:**

This project uses Git Flow. Feature branches must be created from `develop` and merged back into `develop`.

1. Analyze all changes with `git status` and `git diff`
2. Infer an appropriate branch name from the changes:
   - Format: `prefix/kebab-case-description`
   - Allowed prefixes: `feat/`, `fix/`, `update/`, `add/`, `delete/`, `docs/`, `test/`, `init/`
   - Branch prefixes are independent from commit message types. Use `feat/` for feature work even when the commit type is `add`
   - Reflect the domain in the name
   - Examples: `feat/jwt-auth`, `fix/user-query-bug`, `update/optimize-post-query`
3. Create and checkout the branch:
   ```bash
   git checkout -b prefix/inferred-name
   ```
4. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type :: description`

- **Types**: 타입 목록은 `.claude/rules/commit-conventions.md`의 Commit Types 테이블 참조
- **Description**: Korean, no period
  - Good examples: `add :: JWT 토큰 발급 구현`, `fix :: 사용자 조회 쿼리 수정`, `delete :: 불필요한 파일 제거`
- Subject line only (no body)

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Categorize into logical units
3. Group files per unit
4. For each group:
   - Stage only relevant files with `git add`
   - Write a commit message following the rules above
   - `git commit -m "message"`
5. Verify with `git log --oneline -n 5`
