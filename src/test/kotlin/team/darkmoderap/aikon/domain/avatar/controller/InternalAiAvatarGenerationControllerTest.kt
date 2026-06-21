package team.darkmoderap.aikon.domain.avatar.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import team.darkmoderap.aikon.domain.avatar.dto.AvatarGenerationCallbackReqDto
import team.darkmoderap.aikon.domain.avatar.service.HandleAvatarGenerationCallbackService
import team.darkmoderap.aikon.global.common.error.handler.GlobalExceptionHandler

class InternalAiAvatarGenerationControllerTest {
    private val handleAvatarGenerationCallbackService = mock(HandleAvatarGenerationCallbackService::class.java)
    private val callbackSecret = "test-secret"
    private val objectMapper = jacksonObjectMapper()

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(
                InternalAiAvatarGenerationController(
                    handleAvatarGenerationCallbackService,
                    callbackSecret,
                ),
            ).setControllerAdvice(GlobalExceptionHandler())
            .build()

    @Nested
    @DisplayName("POST /internal/ai/avatar-generations/callback 는")
    inner class Callback {
        @Test
        @DisplayName("올바른 시크릿이면 200을 반환하고 서비스를 호출한다")
        fun `returns 200 and calls service when secret is valid`() {
            // Given
            val reqDto = callbackReqDto()

            // When
            val result =
                mockMvc.perform(
                    post("/internal/ai/avatar-generations/callback")
                        .header("X-Internal-Secret", callbackSecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)),
                )

            // Then
            result.andExpect(status().isOk)
            verify(handleAvatarGenerationCallbackService).execute(reqDto)
        }

        @Test
        @DisplayName("시크릿이 일치하지 않으면 401을 반환하고 서비스를 호출하지 않는다")
        fun `returns 401 and does not call service when secret is invalid`() {
            // Given
            val reqDto = callbackReqDto()

            // When
            val result =
                mockMvc.perform(
                    post("/internal/ai/avatar-generations/callback")
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)),
                )

            // Then
            result.andExpect(status().isUnauthorized)
            verify(handleAvatarGenerationCallbackService, never()).execute(reqDto)
        }

        @Test
        @DisplayName("X-Internal-Secret 헤더가 없으면 400을 반환한다")
        fun `returns 400 when X-Internal-Secret header is missing`() {
            // Given
            val reqDto = callbackReqDto()

            // When
            val result =
                mockMvc.perform(
                    post("/internal/ai/avatar-generations/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)),
                )

            // Then
            result.andExpect(status().isBadRequest)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val JOB_ID = "test-job-id"

        private fun callbackReqDto(): AvatarGenerationCallbackReqDto =
            AvatarGenerationCallbackReqDto(
                avatarId = AVATAR_ID,
                jobId = JOB_ID,
                status = "COMPLETED",
                generatedImageUri = "s3://bucket/avatars/1.png",
                modelName = "gemini-2.5-flash",
                promptVersion = "v1",
                promptText = "test prompt",
                durationMs = 3000,
                errorCode = null,
                errorMessage = null,
            )
    }
}
