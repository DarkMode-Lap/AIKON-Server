package team.darkmoderap.aikon.global.common.error

enum class ErrorCode(
    val message: String,
    val status: Int,
) {
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생하였습니다.", 500),
    INVALID_INPUT_VALUE("잘못된 입력 값입니다.", 400),
    METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다.", 405),
    AVATAR_NOT_FOUND("아바타를 찾을 수 없습니다.", 404),
    AVATAR_GENERATION_IN_PROGRESS("아바타가 생성 중이므로 수정할 수 없습니다.", 409),
    AVATAR_PASS_CODE_EXHAUSTED("사용 가능한 아바타 패스 코드가 없습니다.", 409),
    AVATAR_PASS_CODE_ASSIGNMENT_FAILED("아바타 패스 코드 배정에 실패하였습니다.", 409),
    AVATAR_IMAGE_GENERATION_FAILED("아바타 이미지 생성에 실패하였습니다.", 502),
    AVATAR_IMAGE_DELETE_FAILED("아바타 이미지 삭제에 실패하였습니다.", 502),
    AVATAR_IMAGE_URL_GENERATION_FAILED("아바타 이미지 URL 생성에 실패하였습니다.", 502),
    AVATAR_STYLE_NOT_SUPPORTED_BY_AI("해당 스타일은 현재 AI 서버에서 지원되지 않습니다.", 422),
    FASTAPI_REQUEST_FAILED("AI 서버 요청에 실패하였습니다.", 502),
    INVALID_INTERNAL_SECRET("내부 인증에 실패하였습니다.", 401),
    INVALID_AVATAR_JOB_ID("잘못된 AI 작업 식별자입니다.", 400),
    AVATAR_NOT_COMPLETED("아바타 생성이 완료되지 않은 상태입니다.", 409),
    SSE_MAX_CONNECTIONS_EXCEEDED("SSE 최대 연결 수를 초과하였습니다.", 429),
}
