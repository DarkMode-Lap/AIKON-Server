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
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarDefaultStyle
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.repository.AvatarDefaultStyleRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UpdateDefaultStyleServiceImplTest {
    @Mock
    private lateinit var avatarDefaultStyleRepository: AvatarDefaultStyleRepository

    @InjectMocks
    private lateinit var updateDefaultStyleService: UpdateDefaultStyleServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("설정이 있으면 기본 스타일을 수정한다")
        fun `updates default style when setting exists`() {
            // Given
            val setting = AvatarDefaultStyle(defaultStyle = Style.STUDIO)
            given(avatarDefaultStyleRepository.findById(AvatarDefaultStyle.SINGLETON_ID))
                .willReturn(Optional.of(setting))
            val reqDto = UpdateDefaultStyleReqDto(Style.GHIBLI)

            // When
            updateDefaultStyleService.execute(reqDto)

            // Then
            assertEquals(Style.GHIBLI, setting.defaultStyle)
            verify(avatarDefaultStyleRepository, never()).save(any())
        }

        @Test
        @DisplayName("설정이 없으면 서버 내부 오류를 던진다")
        fun `throws internal server error when setting not found`() {
            // Given
            given(avatarDefaultStyleRepository.findById(AvatarDefaultStyle.SINGLETON_ID))
                .willReturn(Optional.empty())
            val reqDto = UpdateDefaultStyleReqDto(Style.GHIBLI)

            // When
            val exception =
                assertThrows<AikonException> {
                    updateDefaultStyleService.execute(reqDto)
                }

            // Then
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.errorCode)
            verify(avatarDefaultStyleRepository, never()).save(any())
        }
    }
}
