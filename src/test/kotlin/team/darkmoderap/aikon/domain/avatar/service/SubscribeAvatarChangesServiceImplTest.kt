package team.darkmoderap.aikon.domain.avatar.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException

@ExtendWith(MockitoExtension::class)
class SubscribeAvatarChangesServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var subscribeAvatarChangesService: SubscribeAvatarChangesServiceImpl

    @BeforeEach
    fun setUp() {
        subscribeAvatarChangesService =
            SubscribeAvatarChangesServiceImpl(
                avatarRepository = avatarRepository,
                eventPublisher = eventPublisher,
                timeoutMillis = 5000L,
                maxConnections = 2,
            )
    }

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("구독을 시작하면 emitter를 이벤트로 발행한다")
        fun `publishes emitter event when subscription starts`() {
            // Given — no setup needed

            // When
            val emitter = subscribeAvatarChangesService.execute()

            // Then
            verify(eventPublisher).publishEvent(emitter)
        }

        @Test
        @DisplayName("최대 연결 수를 초과하면 예외를 던진다")
        fun `throws exception when max connections exceeded`() {
            // Given
            subscribeAvatarChangesService.execute()
            subscribeAvatarChangesService.execute()

            // When & Then
            assertThatThrownBy { subscribeAvatarChangesService.execute() }
                .isInstanceOf(AikonException::class.java)
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
            verify(avatarRepository).findAllByOrderByIdAsc()
        }
    }

    @Nested
    @DisplayName("sendHeartbeat 메서드는")
    inner class SendHeartbeat {
        @Test
        @DisplayName("연결된 emitter가 없으면 아무것도 전송하지 않는다")
        fun `does nothing when no emitters connected`() {
            // Given — no connections

            // When
            subscribeAvatarChangesService.sendHeartbeat()

            // Then — no exception thrown
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
