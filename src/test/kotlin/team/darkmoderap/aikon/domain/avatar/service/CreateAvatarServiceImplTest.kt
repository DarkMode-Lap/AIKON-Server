package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@ExtendWith(MockitoExtension::class)
class CreateAvatarServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarImageGenerator: AvatarImageGenerator

    @Mock
    private lateinit var avatarImageStorage: AvatarImageStorage

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var createAvatarService: CreateAvatarServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("사용 중인 코드가 없으면 Aikon500을 배정하고 아바타를 생성한다")
        fun `creates avatar with first pass code when no code is used`() {
            // Given
            given(avatarRepository.findAllPassUrls()).willReturn(emptyList())
            given(avatarRepository.saveAndFlush(anyAvatar())).willAnswer { invocation -> invocation.arguments[0] }
            given(avatarImageGenerator.generate(anyImageGenerationCommand()))
                .willReturn(GeneratedAvatarImage(byteArrayOf(4, 5, 6), "image/png"))
            given(avatarImageStorage.upload(anyLong(), anyGeneratedImage())).willReturn(IMAGE_URL)
            val reqDto = createReqDto()

            // When
            val result = createAvatarService.execute(reqDto, image())

            // Then
            val avatarCaptor = ArgumentCaptor.forClass(AvatarEntity::class.java)
            verify(avatarRepository).saveAndFlush(avatarCaptor.capture())
            assertEquals("Aikon500", avatarCaptor.value.passUrl)
            assertEquals(GenerationStatus.COMPLETED, result.generationStatus)
            assertEquals(IMAGE_URL, avatarCaptor.value.imageUrl)
            verify(eventPublisher).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("앞 번호가 사용 중이면 다음 사용 가능한 코드를 배정한다")
        fun `creates avatar with next pass code when previous codes are used`() {
            // Given
            given(avatarRepository.findAllPassUrls())
                .willReturn(listOf("Aikon500", "Aikon501"))
            given(avatarRepository.saveAndFlush(anyAvatar())).willAnswer { invocation -> invocation.arguments[0] }
            given(avatarImageGenerator.generate(anyImageGenerationCommand()))
                .willReturn(GeneratedAvatarImage(byteArrayOf(4, 5, 6), "image/png"))
            given(avatarImageStorage.upload(anyLong(), anyGeneratedImage())).willReturn(IMAGE_URL)
            val reqDto = createReqDto()

            // When
            createAvatarService.execute(reqDto, image())

            // Then
            val avatarCaptor = ArgumentCaptor.forClass(AvatarEntity::class.java)
            verify(avatarRepository).saveAndFlush(avatarCaptor.capture())
            assertEquals("Aikon502", avatarCaptor.value.passUrl)
        }

        @Test
        @DisplayName("모든 코드가 사용 중이면 409 예외를 던진다")
        fun `throws conflict when all pass codes are used`() {
            // Given
            val passCodes = (500..899).map { code -> "Aikon$code" }
            given(avatarRepository.findAllPassUrls()).willReturn(passCodes)
            val reqDto = createReqDto()

            // When
            val exception =
                assertThrows<AikonException> {
                    createAvatarService.execute(reqDto, image())
                }

            // Then
            assertEquals(ErrorCode.AVATAR_PASS_CODE_EXHAUSTED, exception.errorCode)
        }

        @Test
        @DisplayName("패스 코드 중복 저장이 발생하면 409 예외를 던진다")
        fun `throws conflict when pass code save conflicts`() {
            // Given
            given(avatarRepository.findAllPassUrls()).willReturn(emptyList())
            given(avatarRepository.saveAndFlush(anyAvatar()))
                .willThrow(DataIntegrityViolationException("duplicate pass code"))
            val reqDto = createReqDto()

            // When
            val exception =
                assertThrows<AikonException> {
                    createAvatarService.execute(reqDto, image())
                }

            // Then
            assertEquals(ErrorCode.AVATAR_PASS_CODE_ASSIGNMENT_FAILED, exception.errorCode)
        }

        @Test
        @DisplayName("생성 성공 시 이미지 생성 결과를 저장하고 목록 변경 이벤트를 발행한다")
        fun `stores generated image and publishes avatar list changed event`() {
            // Given
            given(avatarRepository.findAllPassUrls()).willReturn(emptyList())
            given(avatarRepository.saveAndFlush(anyAvatar())).willReturn(avatar("Aikon500", id = AVATAR_ID))
            given(avatarImageGenerator.generate(anyImageGenerationCommand()))
                .willReturn(GeneratedAvatarImage(byteArrayOf(4, 5, 6), "image/png"))
            given(avatarImageStorage.upload(anyLong(), anyGeneratedImage())).willReturn(IMAGE_URL)
            val reqDto = createReqDto()

            // When
            val result = createAvatarService.execute(reqDto, image())

            // Then
            val eventCaptor = ArgumentCaptor.forClass(Any::class.java)
            verify(eventPublisher).publishEvent(eventCaptor.capture())
            assertTrue(eventCaptor.allValues.any { event -> event is AvatarListChangedEvent })
            assertEquals(GenerationStatus.COMPLETED, result.generationStatus)
        }

        @Test
        @DisplayName("이미지 생성에 실패하면 실패 상태를 반환하고 목록 변경 이벤트를 발행한다")
        fun `returns failed when image generation fails`() {
            // Given
            given(avatarRepository.findAllPassUrls()).willReturn(emptyList())
            given(avatarRepository.saveAndFlush(anyAvatar())).willAnswer { invocation -> invocation.arguments[0] }
            given(avatarImageGenerator.generate(anyImageGenerationCommand()))
                .willThrow(AikonException(ErrorCode.AVATAR_IMAGE_GENERATION_FAILED))
            val reqDto = createReqDto()

            // When
            val result = createAvatarService.execute(reqDto, image())

            // Then
            assertEquals(GenerationStatus.FAILED, result.generationStatus)
            verify(eventPublisher).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("이미지 파일이 비어 있으면 400 예외를 던진다")
        fun `throws invalid input when image is empty`() {
            // Given
            val reqDto = createReqDto()
            val image = MockMultipartFile("image", "avatar.png", "image/png", byteArrayOf())

            // When
            val exception =
                assertThrows<AikonException> {
                    createAvatarService.execute(reqDto, image)
                }

            // Then
            assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
        }

        @Test
        @DisplayName("이미지 콘텐츠 타입이 아니면 400 예외를 던진다")
        fun `throws invalid input when content type is not image`() {
            // Given
            val reqDto = createReqDto()
            val image = MockMultipartFile("image", "avatar.txt", "text/plain", byteArrayOf(1, 2, 3))

            // When
            val exception =
                assertThrows<AikonException> {
                    createAvatarService.execute(reqDto, image)
                }

            // Then
            assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val IMAGE_URL = "https://cdn.example.com/avatars/1.png"

        private fun createReqDto(): CreateAvatarReqDto =
            CreateAvatarReqDto(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
            )

        private fun avatar(
            passUrl: String,
            id: Long = 0,
        ): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                passUrl = passUrl,
                id = id,
            )

        private fun anyAvatar(): AvatarEntity {
            any(AvatarEntity::class.java)
            return avatar("Aikon500")
        }

        private fun anyLong(): Long {
            org.mockito.ArgumentMatchers.anyLong()
            return 0L
        }

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

        private fun image(): MultipartFile =
            MockMultipartFile(
                "image",
                "avatar.png",
                "image/png",
                byteArrayOf(1, 2, 3),
            )

        private fun anyEvent(): Any {
            any(Any::class.java)
            return Any()
        }
    }
}
