package team.darkmoderap.aikon.global.common.error.dto

import team.darkmoderap.aikon.global.common.error.ErrorCode

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse =
            ErrorResponse(
                status = errorCode.status,
                code = errorCode.name,
                message = errorCode.message,
            )
    }
}
