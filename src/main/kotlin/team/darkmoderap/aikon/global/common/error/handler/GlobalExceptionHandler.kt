package team.darkmoderap.aikon.global.common.error.handler

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled validation exception {}", exception.message)

        val errors =
            exception.bindingResult.fieldErrors.map { fieldError ->
                ErrorResponse.FieldError(
                    field = fieldError.field,
                    value = fieldError.rejectedValue?.toString() ?: "",
                    reason = fieldError.defaultMessage ?: "",
                )
            }

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT_VALUE.status)
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(exception: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled method not supported exception {}", exception.message)

        return createErrorResponse(ErrorCode.METHOD_NOT_ALLOWED)
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
