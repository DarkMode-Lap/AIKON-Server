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
}
