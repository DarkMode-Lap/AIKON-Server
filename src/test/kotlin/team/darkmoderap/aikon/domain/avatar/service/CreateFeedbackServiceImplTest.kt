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
import team.darkmoderap.aikon.domain.avatar.dto.CreateFeedbackReqDto
import team.darkmoderap.aikon.domain.avatar.dto.FastApiFeedbackReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.AvatarFeedback
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.FeedbackRating
import team.darkmoderap.aikon.domain.avatar.entity.enum.FeedbackReason
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.repository.AvatarFeedbackRepository
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.client.FastApiClient
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CreateFeedbackServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarFeedbackRepository: AvatarFeedbackRepository

    @Mock
    private lateinit var fastApiClient: FastApiClient

    @InjectMocks
    private lateinit var createFeedbackService: CreateFeedbackServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("생성 완료된 아바타에 피드백을 저장하고 FastAPI로 전송한다")
        fun `saves feedback and sends to fastapi when avatar is completed`() {
            // Given
            val avatar = completedAvatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarFeedbackRepository.findByAvatarId(AVATAR_ID)).willReturn(null)
            given(avatarFeedbackRepository.save(anyAvatarFeedback())).willReturn(
                AvatarFeedback(
                    avatar = avatar,
                    rating = FeedbackRating.LIKE,
                    reasons = listOf(FeedbackReason.FACE_SIMILARITY),
                ),
            )

            // When
            createFeedbackService.execute(AVATAR_ID, reqDto())

            // Then
            verify(avatarFeedbackRepository).save(anyAvatarFeedback())
            verify(fastApiClient).sendFeedback(anyFastApiFeedbackReqDto())
        }

        @Test
        @DisplayName("이미 피드백이 존재하면 업데이트한다")
        fun `updates existing feedback when feedback already exists`() {
            // Given
            val avatar = completedAvatar()
            val existing = existingFeedback(avatar)
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarFeedbackRepository.findByAvatarId(AVATAR_ID)).willReturn(existing)

            // When
            createFeedbackService.execute(AVATAR_ID, reqDto())

            // Then
            verify(avatarFeedbackRepository, never()).save(anyAvatarFeedback())
            verify(fastApiClient).sendFeedback(anyFastApiFeedbackReqDto())
        }

        @Test
        @DisplayName("아바타 생성이 완료되지 않으면 409 예외를 던진다")
        fun `throws 409 when avatar generation is not completed`() {
            // Given
            val avatar = processingAvatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))

            // When
            val exception =
                assertThrows<AikonException> {
                    createFeedbackService.execute(AVATAR_ID, reqDto())
                }

            // Then
            assert(exception.errorCode == ErrorCode.AVATAR_NOT_COMPLETED)
            verify(avatarFeedbackRepository, never()).save(any())
            verify(fastApiClient, never()).sendFeedback(anyFastApiFeedbackReqDto())
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던진다")
        fun `throws 404 when avatar does not exist`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.empty())

            // When
            val exception =
                assertThrows<AikonException> {
                    createFeedbackService.execute(AVATAR_ID, reqDto())
                }

            // Then
            assert(exception.errorCode == ErrorCode.AVATAR_NOT_FOUND)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val JOB_ID = "test-job-id"
        private const val IMAGE_URL = "https://cdn.example.com/avatars/1.png"

        private fun completedAvatar(): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = GenerationStatus.COMPLETED,
                passUrl = "Aikon500",
                imageUrl = IMAGE_URL,
                aiJobId = JOB_ID,
                id = AVATAR_ID,
            )

        private fun processingAvatar(): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = GenerationStatus.PROCESSING,
                passUrl = "Aikon500",
                id = AVATAR_ID,
            )

        private fun existingFeedback(avatar: AvatarEntity): AvatarFeedback =
            AvatarFeedback(
                avatar = avatar,
                rating = FeedbackRating.DISLIKE,
                reasons = listOf(FeedbackReason.AGE_MISMATCH),
                id = 1L,
            )

        private fun reqDto(): CreateFeedbackReqDto =
            CreateFeedbackReqDto(
                rating = FeedbackRating.LIKE,
                reasons = listOf(FeedbackReason.FACE_SIMILARITY),
                comment = null,
                trainingConsent = true,
                feedbackUseConsent = true,
            )

        private fun anyAvatarFeedback(): AvatarFeedback {
            any(AvatarFeedback::class.java)
            return AvatarFeedback(
                avatar = completedAvatar(),
                rating = FeedbackRating.LIKE,
                reasons = listOf(FeedbackReason.FACE_SIMILARITY),
            )
        }

        private fun anyFastApiFeedbackReqDto(): FastApiFeedbackReqDto {
            any(FastApiFeedbackReqDto::class.java)
            return FastApiFeedbackReqDto(
                avatarId = AVATAR_ID,
                jobId = JOB_ID,
                rating = "LIKE",
                reasons = listOf("FACE_SIMILARITY"),
                comment = null,
                trainingConsent = true,
                feedbackUseConsent = true,
                style = "GHIBLI",
                gender = "FEMALE",
                ageRange = "AGE_20_PLUS",
                promptVersion = null,
                modelName = null,
            )
        }
    }
}
