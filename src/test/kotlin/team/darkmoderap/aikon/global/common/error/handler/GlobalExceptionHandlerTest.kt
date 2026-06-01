package team.darkmoderap.aikon.global.common.error.handler

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

class GlobalExceptionHandlerTest {
    private val mockMvc =
        MockMvcBuilders
            .standaloneSetup(TestController())
            .setControllerAdvice(GlobalExceptionHandler())
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
}
