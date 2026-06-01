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
import org.mockito.ArgumentMatchers.anyCollection
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarImageGenerationRequestedEvent
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@ExtendWith(MockitoExtension::class)
class CreateAvatarServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

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
            given(avatarRepository.findAllByPassUrlIn(anyPassCodes())).willReturn(emptyList())
            given(avatarRepository.save(anyAvatar())).willAnswer { invocation -> invocation.arguments[0] }
            val reqDto = createReqDto()

            // When
            val result = createAvatarService.execute(reqDto)

            // Then
            val avatarCaptor = ArgumentCaptor.forClass(AvatarEntity::class.java)
            verify(avatarRepository).save(avatarCaptor.capture())
            assertEquals("Aikon500", avatarCaptor.value.passUrl)
            assertEquals(GenerationStatus.WAITING, result.generationStatus)
            verify(eventPublisher, times(2)).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("앞 번호가 사용 중이면 다음 사용 가능한 코드를 배정한다")
        fun `creates avatar with next pass code when previous codes are used`() {
            // Given
            given(avatarRepository.findAllByPassUrlIn(anyPassCodes()))
                .willReturn(listOf(avatar("Aikon500"), avatar("Aikon501")))
            given(avatarRepository.save(anyAvatar())).willAnswer { invocation -> invocation.arguments[0] }
            val reqDto = createReqDto()

            // When
            createAvatarService.execute(reqDto)

            // Then
            val avatarCaptor = ArgumentCaptor.forClass(AvatarEntity::class.java)
            verify(avatarRepository).save(avatarCaptor.capture())
            assertEquals("Aikon502", avatarCaptor.value.passUrl)
        }

        @Test
        @DisplayName("모든 코드가 사용 중이면 409 예외를 던진다")
        fun `throws conflict when all pass codes are used`() {
            // Given
            val avatars = (500..899).map { code -> avatar("Aikon$code") }
            given(avatarRepository.findAllByPassUrlIn(anyPassCodes())).willReturn(avatars)
            val reqDto = createReqDto()

            // When
            val exception =
                assertThrows<AikonException> {
                    createAvatarService.execute(reqDto)
                }

            // Then
            assertEquals(ErrorCode.AVATAR_PASS_CODE_EXHAUSTED, exception.errorCode)
        }

        @Test
        @DisplayName("생성 성공 시 이미지 생성 요청과 목록 변경 이벤트를 발행한다")
        fun `publishes image generation and avatar list changed events`() {
            // Given
            given(avatarRepository.findAllByPassUrlIn(anyPassCodes())).willReturn(emptyList())
            given(avatarRepository.save(anyAvatar())).willReturn(avatar("Aikon500", id = AVATAR_ID))
            val reqDto = createReqDto()

            // When
            createAvatarService.execute(reqDto)

            // Then
            val eventCaptor = ArgumentCaptor.forClass(Any::class.java)
            verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture())
            assertTrue(eventCaptor.allValues.any { event -> event is AvatarImageGenerationRequestedEvent })
            assertTrue(eventCaptor.allValues.any { event -> event is AvatarListChangedEvent })
        }
    }

    companion object {
        private const val AVATAR_ID = 1L

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

        private fun anyPassCodes(): Collection<String> {
            anyCollection<String>()
            return emptyList()
        }

        private fun anyAvatar(): AvatarEntity {
            any(AvatarEntity::class.java)
            return avatar("Aikon500")
        }

        private fun anyEvent(): Any {
            any(Any::class.java)
            return Any()
        }
    }
}
