package team.darkmoderap.aikon.global.common.error

class AikonException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
