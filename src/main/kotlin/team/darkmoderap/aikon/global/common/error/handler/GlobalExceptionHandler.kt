package team.darkmoderap.aikon.global.common.error.handler

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import team.darkmoderap.aikon.global.common.error.dto.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AikonException::class)
    fun handleAikonException(exception: AikonException): ResponseEntity<ErrorResponse> {
        val errorCode = exception.errorCode
        val message = exception.message ?: errorCode.message

        logger.warn("Handled application exception {} {}", errorCode.name, message)

        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(errorCode, message))
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(exception: BindException): ResponseEntity<ErrorResponse> {
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

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(exception: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled not readable request exception {}", exception.message)

        return createErrorResponse(ErrorCode.INVALID_INPUT_VALUE)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(exception: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled type mismatch exception {}", exception.message)

        return createErrorResponse(ErrorCode.INVALID_INPUT_VALUE)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(exception: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled method not supported exception {}", exception.message)

        return createErrorResponse(ErrorCode.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(exception: AccessDeniedException): Nothing = throw exception

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
