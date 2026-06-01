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
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository

@ExtendWith(MockitoExtension::class)
class DeleteAvatarServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @InjectMocks
    private lateinit var deleteAvatarService: DeleteAvatarServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("아바타가 존재하면 삭제한다")
        fun `deletes avatar when it exists`() {
            // Given
            given(avatarRepository.existsById(AVATAR_ID)).willReturn(true)

            // When
            deleteAvatarService.execute(AVATAR_ID)

            // Then
            verify(avatarRepository).deleteById(AVATAR_ID)
        }

        @Test
        @DisplayName("아바타가 존재하지 않으면 404 예외를 던지고 삭제하지 않는다")
        fun `throws not found when avatar does not exist`() {
            // Given
            given(avatarRepository.existsById(AVATAR_ID)).willReturn(false)

            // When
            val exception =
                assertThrows<ResponseStatusException> {
                    deleteAvatarService.execute(AVATAR_ID)
                }

            // Then
            assertEquals(HttpStatus.NOT_FOUND.value(), exception.statusCode.value())
            verify(avatarRepository, never()).deleteById(anyLong())
        }
    }

    companion object {
        private const val AVATAR_ID = 1L
    }
}
