package team.darkmoderap.aikon.global.common.error

class AikonException(
    val errorCode: ErrorCode,
    message: String = errorCode.message,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
