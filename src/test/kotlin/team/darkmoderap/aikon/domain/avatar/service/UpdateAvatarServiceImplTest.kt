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
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UpdateAvatarServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @InjectMocks
    private lateinit var updateAvatarService: UpdateAvatarServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("아바타가 존재하고 생성 중이 아니면 정보를 수정한다")
        fun `updates avatar when found and not generating`() {
            // Given
            val avatar = avatar(GenerationStatus.COMPLETED)
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            val reqDto = UpdateAvatarReqDto("새이름", Gender.FEMALE, AgeRange.AGE_20_PLUS)

            // When
            updateAvatarService.execute(AVATAR_ID, reqDto)

            // Then
            assertEquals("새이름", avatar.nickname)
            assertEquals(Gender.FEMALE, avatar.gender)
            assertEquals(AgeRange.AGE_20_PLUS, avatar.ageRange)
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던진다")
        fun `throws not found when avatar does not exist`() {
            // Given
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.empty())
            val reqDto = UpdateAvatarReqDto("새이름", Gender.FEMALE, AgeRange.AGE_20_PLUS)

            // When
            val exception =
                assertThrows<ResponseStatusException> {
                    updateAvatarService.execute(AVATAR_ID, reqDto)
                }

            // Then
            assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode.value())
        }

        @Test
        @DisplayName("아바타가 생성 진행 중이면 409 예외를 던진다")
        fun `throws conflict when avatar is generating`() {
            // Given
            val avatar = avatar(GenerationStatus.PROCESSING)
            given(avatarRepository.findById(AVATAR_ID)).willReturn(Optional.of(avatar))
            val reqDto = UpdateAvatarReqDto("새이름", Gender.FEMALE, AgeRange.AGE_20_PLUS)

            // When
            val exception =
                assertThrows<ResponseStatusException> {
                    updateAvatarService.execute(AVATAR_ID, reqDto)
                }

            // Then
            assertEquals(HttpStatus.CONFLICT.value(), exception.statusCode.value())
        }
    }

    companion object {
        private const val AVATAR_ID = 1L

        private fun avatar(generationStatus: GenerationStatus): AvatarEntity =
            AvatarEntity(
                nickname = "기존이름",
                gender = Gender.MALE,
                style = Style.STUDIO,
                ageRange = AgeRange.AGE_0_7,
                generationStatus = generationStatus,
            )
    }
}
