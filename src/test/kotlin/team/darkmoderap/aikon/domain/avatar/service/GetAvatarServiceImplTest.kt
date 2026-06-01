package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
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
class GetAvatarServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @InjectMocks
    private lateinit var getAvatarService: GetAvatarServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("아바타가 존재하면 조회 응답을 반환한다")
        fun `returns avatar when found`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar()))

            // When
            val result = getAvatarService.execute(AVATAR_ID)

            // Then
            assertEquals(AVATAR_ID, result.id)
            assertEquals("새아바타", result.nickname)
            assertEquals("https://example.com/avatar.png", result.imageUrl)
            assertEquals("Aikon500", result.passUrl)
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던진다")
        fun `throws not found when avatar does not exist`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.empty())

            // When
            val exception =
                assertThrows<AikonException> {
                    getAvatarService.execute(AVATAR_ID)
                }

            // Then
            assertEquals(ErrorCode.AVATAR_NOT_FOUND, exception.errorCode)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L

        private fun avatar(): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = GenerationStatus.COMPLETED,
                imageUrl = "https://example.com/avatar.png",
                passUrl = "Aikon500",
                id = AVATAR_ID,
            )
    }
}
