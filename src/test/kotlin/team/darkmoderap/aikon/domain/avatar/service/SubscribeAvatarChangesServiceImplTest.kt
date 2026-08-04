package team.darkmoderap.aikon.domain.avatar.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.event.AvatarSseSubscribedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.domain.avatar.repository.AvatarSummaryProjection
import team.darkmoderap.aikon.global.common.error.AikonException
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class SubscribeAvatarChangesServiceImplTest {
    @Mock
    private lateinit var avatarRepository: AvatarRepository

    @Mock
    private lateinit var avatarImageStorage: AvatarImageStorage

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Mock
    private lateinit var transactionManager: PlatformTransactionManager

    @Mock
    private lateinit var transactionStatus: TransactionStatus

    private lateinit var subscribeAvatarChangesService: SubscribeAvatarChangesServiceImpl

    @BeforeEach
    fun setUp() {
        subscribeAvatarChangesService =
            SubscribeAvatarChangesServiceImpl(
                avatarRepository = avatarRepository,
                avatarImageStorage = avatarImageStorage,
                eventPublisher = eventPublisher,
                transactionManager = transactionManager,
                timeoutMillis = 5000L,
                maxConnections = 2,
            )
    }

    @Nested
    @DisplayName("execute 메서드는")
    inner class Execute {
        @Test
        @DisplayName("구독을 시작하면 AvatarSseSubscribedEvent를 발행한다")
        fun `publishes AvatarSseSubscribedEvent when subscription starts`() {
            // Given — no setup needed

            // When
            subscribeAvatarChangesService.execute()

            // Then
            verify(eventPublisher).publishEvent(any(AvatarSseSubscribedEvent::class.java))
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
            given(transactionManager.getTransaction(any(TransactionDefinition::class.java))).willReturn(transactionStatus)
            given(avatarRepository.findAllByOrderByIdAsc()).willReturn(listOf(avatarSummaryProjection()))
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
        private fun avatarSummaryProjection(): AvatarSummaryProjection =
            object : AvatarSummaryProjection {
                override val id: Long = 1L
                override val nickname: String = "새아바타"
                override val style: Style = Style.GHIBLI
                override val gender: Gender = Gender.FEMALE
                override val ageRange: AgeRange = AgeRange.AGE_20_PLUS
                override val generationStatus: GenerationStatus = GenerationStatus.WAITING
                override val imageUrl: String? = null
                override val passUrl: String? = "Aikon500"
                override val createdAt: Instant = Instant.now()
            }
    }
}
