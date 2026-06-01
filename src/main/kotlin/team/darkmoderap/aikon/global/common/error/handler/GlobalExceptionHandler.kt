package team.darkmoderap.aikon.global.common.error.handler

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import team.darkmoderap.aikon.global.common.error.dto.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AikonException::class)
    fun handleAikonException(exception: AikonException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled application exception {}", exception.errorCode.name)

        return createErrorResponse(exception.errorCode)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Handled unexpected exception", exception)

        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun createErrorResponse(errorCode: ErrorCode): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(errorCode))
}
