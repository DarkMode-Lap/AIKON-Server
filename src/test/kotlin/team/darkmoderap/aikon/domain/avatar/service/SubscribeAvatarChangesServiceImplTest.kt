package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository

@ExtendWith(MockitoExtension::class)
class SubscribeAvatarChangesServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @InjectMocks
    private lateinit var subscribeAvatarChangesService: SubscribeAvatarChangesServiceImpl

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("구독을 시작하면 현재 아바타 목록을 조회한다")
        fun `finds current avatar list when subscription starts`() {
            // Given
            given(avatarRepository.findAllByOrderByIdAsc()).willReturn(listOf(avatar()))

            // When
            subscribeAvatarChangesService.execute()

            // Then
            verify(avatarRepository).findAllByOrderByIdAsc()
        }
    }

    @Nested
    @DisplayName("handleAvatarListChanged 메서드는")
    inner class HandleAvatarListChanged {
        @Test
        @DisplayName("목록 변경 이벤트를 받으면 최신 아바타 목록을 조회한다")
        fun `finds latest avatar list when event is received`() {
            // Given
            given(avatarRepository.findAllByOrderByIdAsc()).willReturn(listOf(avatar()))
            subscribeAvatarChangesService.execute()

            // When
            subscribeAvatarChangesService.handleAvatarListChanged(AvatarListChangedEvent())

            // Then
            verify(avatarRepository, times(2)).findAllByOrderByIdAsc()
        }
    }

    companion object {
        private fun avatar(): AvatarEntity =
            AvatarEntity(
                nickname = "새아바타",
                gender = Gender.FEMALE,
                style = Style.GHIBLI,
                ageRange = AgeRange.AGE_20_PLUS,
                generationStatus = GenerationStatus.WAITING,
                passUrl = "Aikon500",
            )
    }
}
