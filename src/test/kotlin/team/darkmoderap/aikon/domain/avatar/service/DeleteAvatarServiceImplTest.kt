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
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DeleteAvatarServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var deleteAvatarService: DeleteAvatarServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("아바타가 존재하고 생성 중이 아니면 삭제한다")
        fun `deletes avatar when it exists and not generating`() {
            // Given
            val avatar = buildAvatar(GenerationStatus.COMPLETED)
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))

            // When
            deleteAvatarService.execute(AVATAR_ID)

            // Then
            verify(avatarRepository).delete(avatar)
            verify(eventPublisher).publishEvent(anyEvent())
        }

        @Test
        @DisplayName("아바타가 생성 중이면 409 예외를 던지고 삭제하지 않는다")
        fun `throws conflict when avatar is generating`() {
            // Given
            val avatar = buildAvatar(GenerationStatus.PROCESSING)
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))

            // When
            val exception =
                assertThrows<AikonException> {
                    deleteAvatarService.execute(AVATAR_ID)
                }

            // Then
            assertEquals(ErrorCode.AVATAR_GENERATION_IN_PROGRESS, exception.errorCode)
            verify(avatarRepository, never()).delete(any())
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던지고 삭제하지 않는다")
        fun `throws not found when avatar does not exist`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.empty())

            // When
            val exception =
                assertThrows<AikonException> {
                    deleteAvatarService.execute(AVATAR_ID)
                }

            // Then
            assertEquals(ErrorCode.AVATAR_NOT_FOUND, exception.errorCode)
            verify(avatarRepository, never()).delete(any())
        }
    }

    companion object {
        private const val AVATAR_ID = 1L

        private fun buildAvatar(status: GenerationStatus) =
            AvatarEntity(
                nickname = "test",
                gender = Gender.MALE,
                style = Style.STUDIO,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = status,
            )

        private fun anyEvent(): Any {
            any(Any::class.java)
            return Any()
        }
    }
}
