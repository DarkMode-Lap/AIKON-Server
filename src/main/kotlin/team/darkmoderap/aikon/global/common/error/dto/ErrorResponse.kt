package team.darkmoderap.aikon.global.common.error.dto

import io.swagger.v3.oas.annotations.media.Schema
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Schema(description = "에러 응답")
data class ErrorResponse(
    @Schema(description = "HTTP 상태 코드", example = "404")
    val status: Int,
    @Schema(description = "에러 코드", example = "AVATAR_NOT_FOUND")
    val code: String,
    @Schema(description = "에러 메시지", example = "아바타를 찾을 수 없습니다.")
    val message: String,
    val errors: List<FieldError> = emptyList(),
) {
    @Schema(description = "필드 유효성 검사 에러")
    data class FieldError(
        @Schema(description = "필드명", example = "nickname")
        val field: String,
        @Schema(description = "입력값", example = "")
        val value: String,
        @Schema(description = "에러 사유", example = "공백일 수 없습니다")
        val reason: String,
    )

    companion object {
        fun of(
            errorCode: ErrorCode,
            message: String = errorCode.message,
        ): ErrorResponse =
            ErrorResponse(
                status = errorCode.status,
                code = errorCode.name,
                message = message,
            )

        fun of(
            errorCode: ErrorCode,
            errors: List<FieldError>,
            message: String = errorCode.message,
        ): ErrorResponse =
            ErrorResponse(
                status = errorCode.status,
                code = errorCode.name,
                message = message,
                errors = errors,
            )
    }
}
