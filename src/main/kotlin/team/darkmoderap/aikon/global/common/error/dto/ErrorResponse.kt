package team.darkmoderap.aikon.global.common.error.dto

import team.darkmoderap.aikon.global.common.error.ErrorCode

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
    val errors: List<FieldError> = emptyList(),
) {
    data class FieldError(
        val field: String,
        val value: String,
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
