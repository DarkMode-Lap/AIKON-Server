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

@ExtendWith(MockitoExtension::class)
class GetAvatarByPassServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarQrUrlProvider: AvatarQrUrlProvider

    @InjectMocks
    private lateinit var getAvatarByPassService: GetAvatarByPassServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("생성이 완료된 아바타가 존재하면 결과 URL을 포함한 조회 응답을 반환한다")
        fun `returns completed avatar with result urls when found`() {
            // Given
            given(avatarRepository.findByPassUrl(PASS_URL)).willReturn(avatar())
            given(avatarQrUrlProvider.create(PASS_URL)).willReturn(QR_URL)

            // When
            val result = getAvatarByPassService.execute(PASS_URL)

            // Then
            assertEquals(AVATAR_ID, result.id)
            assertEquals("새아바타", result.nickname)
            assertEquals(GenerationStatus.COMPLETED, result.generationStatus)
            assertEquals("https://example.com/avatar.png", result.imageUrl)
            assertEquals(PASS_URL, result.passUrl)
            assertEquals(QR_URL, result.qrUrl)
        }

        @Test
        @DisplayName("생성 중인 아바타는 결과 URL을 제외한 조회 응답을 반환한다")
        fun `returns generating avatar without result urls when found`() {
            // Given
            given(avatarRepository.findByPassUrl(PASS_URL)).willReturn(avatar(GenerationStatus.PROCESSING))

            // When
            val result = getAvatarByPassService.execute(PASS_URL)

            // Then
            assertEquals(AVATAR_ID, result.id)
            assertEquals("새아바타", result.nickname)
            assertEquals(GenerationStatus.PROCESSING, result.generationStatus)
            assertEquals(null, result.imageUrl)
            assertEquals(null, result.passUrl)
            assertEquals(null, result.qrUrl)
        }

        @Test
        @DisplayName("패스 URL에 해당하는 아바타가 존재하지 않으면 404 예외를 던진다")
        fun `throws not found when avatar does not exist`() {
            // Given
            given(avatarRepository.findByPassUrl(PASS_URL)).willReturn(null)

            // When
            val exception =
                assertThrows<AikonException> {
                    getAvatarByPassService.execute(PASS_URL)
                }

            // Then
            assertEquals(ErrorCode.AVATAR_NOT_FOUND, exception.errorCode)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
        private const val PASS_URL = "Aikon500"
        private const val QR_URL = "https://aikon.example.com/pass/Aikon500"

        private fun avatar(generationStatus: GenerationStatus = GenerationStatus.COMPLETED): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = generationStatus,
                imageUrl = "https://example.com/avatar.png",
                passUrl = PASS_URL,
                id = AVATAR_ID,
            )
    }
}
