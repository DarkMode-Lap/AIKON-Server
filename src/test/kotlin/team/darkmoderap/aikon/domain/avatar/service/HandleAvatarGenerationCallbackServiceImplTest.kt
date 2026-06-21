package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import team.darkmoderap.aikon.domain.avatar.dto.AvatarGenerationCallbackReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class HandleAvatarGenerationCallbackServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarImageStorage: AvatarImageStorage

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var handleAvatarGenerationCallbackService: HandleAvatarGenerationCallbackServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("COMPLETED 콜백이고 jobId가 일치하면 아바타를 완료 처리하고 SSE 이벤트를 발행한다")
        fun `completes avatar and publishes event when completed callback with matching jobId`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarImageStorage.toPublicUrl(S3_URI)).willReturn(PUBLIC_URL)

            // When
            handleAvatarGenerationCallbackService.execute(completedCallbackReqDto())

            // Then
            verify(eventPublisher).publishEvent(anyEvent())
            assert(avatar.generationStatus == GenerationStatus.COMPLETED)
            assert(avatar.imageUrl == PUBLIC_URL)
        }

        @Test
        @DisplayName("FAILED 콜백이고 jobId가 일치하면 아바타를 실패 처리하고 SSE 이벤트를 발행한다")
        fun `fails avatar and publishes event when failed callback with matching jobId`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))

            // When
            handleAvatarGenerationCallbackService.execute(failedCallbackReqDto())

            // Then
            verify(eventPublisher).publishEvent(anyEvent())
            assert(avatar.generationStatus == GenerationStatus.FAILED)
        }

        @Test
        @DisplayName("jobId가 일치하지 않으면 400 예외를 던진다")
        fun `throws 400 when jobId does not match`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))

            // When
            val exception =
                assertThrows<AikonException> {
                    handleAvatarGenerationCallbackService.execute(
                        completedCallbackReqDto().copy(jobId = "wrong-job-id"),
                    )
                }

            // Then
            assert(exception.errorCode == ErrorCode.INVALID_AVATAR_JOB_ID)
            verify(eventPublisher, never()).publishEvent(any())
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던진다")
        fun `throws 404 when avatar does not exist`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.empty())

            // When
            val exception =
                assertThrows<AikonException> {
                    handleAvatarGenerationCallbackService.execute(completedCallbackReqDto())
                }

            // Then
            assert(exception.errorCode == ErrorCode.AVATAR_NOT_FOUND)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val JOB_ID = "test-job-id"
        private const val S3_URI = "s3://bucket/avatars/1.png"
        private const val PUBLIC_URL = "https://cdn.example.com/avatars/1.png"

        private fun avatar(): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = GenerationStatus.PROCESSING,
                passUrl = "Aikon500",
                aiJobId = JOB_ID,
                id = AVATAR_ID,
            )

        private fun completedCallbackReqDto(): AvatarGenerationCallbackReqDto =
            AvatarGenerationCallbackReqDto(
                avatarId = AVATAR_ID,
                jobId = JOB_ID,
                status = "COMPLETED",
                generatedImageUri = S3_URI,
                modelName = "gemini-2.5-flash",
                promptVersion = "v1",
                promptText = "test prompt",
                durationMs = 3000,
                errorCode = null,
                errorMessage = null,
            )

        private fun failedCallbackReqDto(): AvatarGenerationCallbackReqDto =
            AvatarGenerationCallbackReqDto(
                avatarId = AVATAR_ID,
                jobId = JOB_ID,
                status = "FAILED",
                generatedImageUri = null,
                modelName = null,
                promptVersion = null,
                promptText = null,
                durationMs = null,
                errorCode = "GENERATION_FAILED",
                errorMessage = "generation error",
            )

        private fun anyEvent(): Any {
            any(AvatarListChangedEvent::class.java)
            return AvatarListChangedEvent()
        }
    }
}
