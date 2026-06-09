package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style

class AvatarImagePromptProviderTest {
    private val avatarImagePromptProvider = AvatarImagePromptProvider()

    @Nested
    @DisplayName("getPrompt 메서드는")
    inner class GetPrompt {
        @Test
        @DisplayName("ENHANCED 스타일이면 원본 인물성을 유지한 화려한 프로필 보정 프롬프트를 반환한다")
        fun `returns enhanced prompt`() {
            // Given
            val style = Style.ENHANCED
            val gender = Gender.FEMALE
            val ageRange = AgeRange.AGE_20_PLUS

            // When
            val result = avatarImagePromptProvider.getPrompt(style, gender, ageRange)

            // Then
            assertAll(
                { assertTrue(result.contains("프리미엄 프로필")) },
                { assertTrue(result.contains("동일하게 유지")) },
                { assertTrue(result.contains("세련되게 조정")) },
                { assertTrue(result.contains("고급 프로필/화보")) },
                { assertTrue(result.contains("스타일 변환은 하지 마")) },
            )
        }

        @Test
        @DisplayName("모든 스타일의 프롬프트를 반환한다")
        fun `returns prompt for every style`() {
            // Given
            val gender = Gender.FEMALE
            val ageRange = AgeRange.AGE_20_PLUS

            // When & Then
            Style.entries.forEach { style ->
                val result = avatarImagePromptProvider.getPrompt(style, gender, ageRange)

                assertFalse(result.isBlank())
            }
        }
    }
}
