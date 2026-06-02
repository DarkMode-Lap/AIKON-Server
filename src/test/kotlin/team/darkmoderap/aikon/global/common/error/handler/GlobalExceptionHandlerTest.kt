package team.darkmoderap.aikon.global.common.error.handler

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlobalExceptionHandlerTest {
    private val exceptionHandler = GlobalExceptionHandler()
    private val mockMvc =
        MockMvcBuilders
            .standaloneSetup(TestController())
            .setControllerAdvice(exceptionHandler)
            .build()

    @Nested
    @DisplayName("AikonException 예외 처리는")
    inner class HandleAikonException {
        @Test
        @DisplayName("에러 코드 기준으로 예외 응답을 반환한다")
        fun `returns error response by error code`() {
            // Given
            val errorCode = ErrorCode.INTERNAL_SERVER_ERROR

            // When & Then
            mockMvc
                .get("/test/aikon-exception")
                .andExpect {
                    status { isInternalServerError() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.status") { value(errorCode.status) }
                    jsonPath("$.code") { value(errorCode.name) }
                    jsonPath("$.message") { value(errorCode.message) }
                }
        }

        @Test
        @DisplayName("커스텀 메시지를 예외 응답에 반영한다")
        fun `returns custom exception message`() {
            // Given
            val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
            val message = "요청을 처리할 수 없습니다."

            // When
            val response =
                exceptionHandler.handleAikonException(
                    AikonException(
                        errorCode = errorCode,
                        message = message,
                    ),
                )

            // Then
            assertEquals(errorCode.status, response.statusCode.value())
            assertEquals(errorCode.status, response.body?.status)
            assertEquals(errorCode.name, response.body?.code)
            assertEquals(message, response.body?.message)
        }
    }

    @Nested
    @DisplayName("BindException 예외 처리는")
    inner class HandleBindException {
        @Test
        @DisplayName("필드 오류 목록을 포함한 입력값 오류 응답을 반환한다")
        fun `returns field error response`() {
            // Given
            val errorCode = ErrorCode.INVALID_INPUT_VALUE
            val bindingResult = BeanPropertyBindingResult(TestRequest(name = ""), "reqDto")
            bindingResult.rejectValue("name", "NotBlank", null, "이름을 입력해 주세요.")

            // When
            val response = exceptionHandler.handleBindException(BindException(bindingResult))

            // Then
            val body = assertNotNull(response.body)
            assertEquals(errorCode.status, response.statusCode.value())
            assertEquals(errorCode.status, body.status)
            assertEquals(errorCode.name, body.code)
            assertEquals(errorCode.message, body.message)
            assertEquals("name", body.errors.first().field)
            assertEquals("", body.errors.first().value)
            assertEquals("이름을 입력해 주세요.", body.errors.first().reason)
        }
    }

    @Nested
    @DisplayName("HttpRequestMethodNotSupportedException 예외 처리는")
    inner class HandleHttpRequestMethodNotSupportedException {
        @Test
        @DisplayName("지원하지 않는 HTTP 메서드 응답을 반환한다")
        fun `returns method not allowed response`() {
            // Given
            val errorCode = ErrorCode.METHOD_NOT_ALLOWED

            // When
            val response =
                exceptionHandler.handleHttpRequestMethodNotSupportedException(
                    HttpRequestMethodNotSupportedException("GET"),
                )

            // Then
            assertEquals(errorCode.status, response.statusCode.value())
            assertEquals(errorCode.status, response.body?.status)
            assertEquals(errorCode.name, response.body?.code)
            assertEquals(errorCode.message, response.body?.message)
        }
    }

    @Nested
    @DisplayName("Exception 예외 처리는")
    inner class HandleException {
        @Test
        @DisplayName("서버 내부 오류 응답을 반환한다")
        fun `returns internal server error response`() {
            // Given
            val errorCode = ErrorCode.INTERNAL_SERVER_ERROR

            // When & Then
            mockMvc
                .get("/test/exception")
                .andExpect {
                    status { isInternalServerError() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.status") { value(errorCode.status) }
                    jsonPath("$.code") { value(errorCode.name) }
                    jsonPath("$.message") { value(errorCode.message) }
                }
        }
    }

    @RestController
    private class TestController {
        @GetMapping("/test/aikon-exception")
        fun throwAikonException(): Nothing = throw AikonException(ErrorCode.INTERNAL_SERVER_ERROR)

        @GetMapping("/test/exception")
        fun throwException(): Nothing = throw RuntimeException("Unexpected exception")
    }

    private data class TestRequest(
        val name: String,
    )
}
