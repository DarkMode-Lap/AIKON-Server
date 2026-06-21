package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import team.darkmoderap.aikon.domain.avatar.dto.FastApiGenerationReqDto
import team.darkmoderap.aikon.domain.avatar.dto.FastApiGenerationResDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.client.FastApiClient
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class GenerateAvatarImageServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarImageStorage: AvatarImageStorage

    @Mock
    private lateinit var fastApiClient: FastApiClient

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var generateAvatarImageService: GenerateAvatarImageServiceImpl

    @BeforeEach
    fun setUp() {
        generateAvatarImageService =
            GenerateAvatarImageServiceImpl(
                avatarRepository,
                avatarImageStorage,
                fastApiClient,
                eventPublisher,
                TransactionTemplate(NoOpTransactionManager()),
                API_BASE_URL,
            )
    }

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("소스 이미지 업로드 및 FastAPI 요청에 성공하면 jobId를 저장한다")
        fun `saves jobId when source upload and fastapi request succeed`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarImageStorage.uploadSourceImage(anyLong(), anyByteArray(), anyString()))
                .willReturn(SOURCE_URI)
            given(fastApiClient.requestAvatarGeneration(anyFastApiGenerationReqDto()))
                .willReturn(FastApiGenerationResDto(jobId = JOB_ID, status = "PENDING"))

            // When
            generateAvatarImageService.execute(AVATAR_ID, sourceImage())

            // Then
            verify(fastApiClient).requestAvatarGeneration(anyFastApiGenerationReqDto())
            verify(eventPublisher, never()).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("소스 이미지 업로드에 실패하면 실패 상태로 변경하고 SSE 이벤트를 발행한다")
        fun `fails avatar when source image upload fails`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarImageStorage.uploadSourceImage(anyLong(), anyByteArray(), anyString()))
                .willThrow(RuntimeException("S3 upload failed"))

            // When
            generateAvatarImageService.execute(AVATAR_ID, sourceImage())

            // Then
            verify(eventPublisher).publishEvent(anyEvent())
            verify(fastApiClient, never()).requestAvatarGeneration(anyFastApiGenerationReqDto())
        }

        @Test
        @DisplayName("FastAPI 요청에 실패하면 실패 상태로 변경하고 SSE 이벤트를 발행한다")
        fun `fails avatar when fastapi request fails`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarImageStorage.uploadSourceImage(anyLong(), anyByteArray(), anyString()))
                .willReturn(SOURCE_URI)
            given(fastApiClient.requestAvatarGeneration(anyFastApiGenerationReqDto()))
                .willThrow(AikonException(ErrorCode.FASTAPI_REQUEST_FAILED))

            // When
            generateAvatarImageService.execute(AVATAR_ID, sourceImage())

            // Then
            verify(eventPublisher).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던진다")
        fun `throws not found when avatar does not exist`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.empty())

            // When
            val exception =
                assertThrows<AikonException> {
                    generateAvatarImageService.execute(AVATAR_ID, sourceImage())
                }

            // Then
            assert(exception.errorCode == ErrorCode.AVATAR_NOT_FOUND)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val JOB_ID = "test-job-id"
        private const val SOURCE_URI = "s3://bucket/sources/1.png"
        private const val API_BASE_URL = "https://api.aikon.example.com"

        private fun avatar(): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = GenerationStatus.PROCESSING,
                passUrl = "Aikon500",
                id = AVATAR_ID,
            )

        private fun sourceImage(): AvatarSourceImage =
            AvatarSourceImage(
                bytes = byteArrayOf(1, 2, 3),
                mimeType = "image/png",
            )

        private fun anyFastApiGenerationReqDto(): FastApiGenerationReqDto {
            any(FastApiGenerationReqDto::class.java)
            return FastApiGenerationReqDto(
                avatarId = AVATAR_ID,
                sourceImageUri = SOURCE_URI,
                style = "GHIBLI",
                gender = "FEMALE",
                ageRange = "AGE_20_PLUS",
                callbackUrl = "$API_BASE_URL/internal/ai/avatar-generations/callback",
            )
        }

        private fun anyByteArray(): ByteArray {
            any(ByteArray::class.java)
            return byteArrayOf()
        }

        private fun anyEvent(): Any {
            any(AvatarListChangedEvent::class.java)
            return AvatarListChangedEvent()
        }
    }

    private class NoOpTransactionManager : PlatformTransactionManager {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus = SimpleTransactionStatus()

        override fun commit(status: TransactionStatus) = Unit

        override fun rollback(status: TransactionStatus) = Unit
    }
}
