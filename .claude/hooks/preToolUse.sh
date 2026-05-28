#!/bin/bash

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    if [[ -n "$CWD" ]]; then
        LOG_FILE="$CWD/.claude/command.log"
        mkdir -p "$(dirname "$LOG_FILE")"
        echo "[$TIMESTAMP] $COMMAND" >> "$LOG_FILE"
    fi

    # heredoc 이후 내용(인자값)은 제외하고 첫 줄 명령어만 검사
    FIRST_LINE=$(echo "$COMMAND" | head -1)

    BLOCKED_PATTERNS=(
        "rm -rf[[:space:]]+/"
        "sudo[[:space:]]+rm"
        ">[[:space:]]*/dev/"
        "dd[[:space:]]+if="
        "mkfs"
        "curl.*\|[[:space:]]*sh"
        "wget.*\|[[:space:]]*sh"
    )
    for pattern in "${BLOCKED_PATTERNS[@]}"; do
        if [[ "$FIRST_LINE" =~ $pattern ]]; then
            echo "[Hook] Blocked dangerous command: $FIRST_LINE" >&2
            exit 2
        fi
    done
fi

exit 0
