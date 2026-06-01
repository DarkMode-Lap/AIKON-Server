package team.darkmoderap.aikon.global.common.error

enum class ErrorCode(
    val message: String,
    val status: Int,
) {
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생하였습니다.", 500),
    INVALID_INPUT_VALUE("잘못된 입력 값입니다.", 400),
    METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다.", 405),
}
