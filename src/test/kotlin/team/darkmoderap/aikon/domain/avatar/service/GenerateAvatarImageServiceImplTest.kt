package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
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
class GenerateAvatarImageServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarImageGenerator: AvatarImageGenerator

    @Mock
    private lateinit var avatarImageStorage: AvatarImageStorage

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var generateAvatarImageService: GenerateAvatarImageServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("이미지 생성에 성공하면 이미지 URL을 저장하고 완료 상태로 변경한다")
        fun `completes avatar when image generation succeeds`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarImageGenerator.generate(anyImageGenerationCommand()))
                .willReturn(GeneratedAvatarImage(byteArrayOf(4, 5, 6), "image/png"))
            given(avatarImageStorage.upload(anyLong(), anyGeneratedImage())).willReturn(IMAGE_URL)

            // When
            generateAvatarImageService.execute(AVATAR_ID, sourceImage())

            // Then
            assertEquals(GenerationStatus.COMPLETED, avatar.generationStatus)
            assertEquals(IMAGE_URL, avatar.imageUrl)
            verify(eventPublisher).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("이미지 생성에 실패하면 실패 상태로 변경한다")
        fun `fails avatar when image generation fails`() {
            // Given
            val avatar = avatar()
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            given(avatarImageGenerator.generate(anyImageGenerationCommand()))
                .willThrow(AikonException(ErrorCode.AVATAR_IMAGE_GENERATION_FAILED))

            // When
            generateAvatarImageService.execute(AVATAR_ID, sourceImage())

            // Then
            assertEquals(GenerationStatus.FAILED, avatar.generationStatus)
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
            assertEquals(ErrorCode.AVATAR_NOT_FOUND, exception.errorCode)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val IMAGE_URL = "https://cdn.example.com/avatars/1.png"

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

        private fun anyImageGenerationCommand(): AvatarImageGenerationCommand {
            any(AvatarImageGenerationCommand::class.java)
            return AvatarImageGenerationCommand(
                style = Style.GHIBLI,
                sourceImage = byteArrayOf(1, 2, 3),
                sourceMimeType = "image/png",
            )
        }

        private fun anyGeneratedImage(): GeneratedAvatarImage {
            any(GeneratedAvatarImage::class.java)
            return GeneratedAvatarImage(byteArrayOf(4, 5, 6), "image/png")
        }

        private fun anyLong(): Long {
            org.mockito.ArgumentMatchers.anyLong()
            return 0L
        }

        private fun anyEvent(): Any {
            any(AvatarListChangedEvent::class.java)
            return AvatarListChangedEvent()
        }
    }
}
