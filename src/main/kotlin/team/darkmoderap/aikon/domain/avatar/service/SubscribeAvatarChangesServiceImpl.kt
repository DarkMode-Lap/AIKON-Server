package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.darkmoderap.aikon.domain.avatar.dto.AvatarChangeResDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.event.AvatarSseSubscribedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

@Service
class SubscribeAvatarChangesServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${aikon.sse.timeout-millis:1800000}") private val timeoutMillis: Long,
    @Value("\${aikon.sse.max-connections:100}") private val maxConnections: Int,
) : SubscribeAvatarChangesService {
    private val logger = LoggerFactory.getLogger(SubscribeAvatarChangesServiceImpl::class.java)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    override fun execute(): SseEmitter {
        if (emitters.size >= maxConnections) {
            throw AikonException(ErrorCode.SSE_MAX_CONNECTIONS_EXCEEDED)
        }
        val emitter = SseEmitter(timeoutMillis)
        emitters.add(emitter)

        emitter.onCompletion { cleanup(emitter, "closed") }
        emitter.onTimeout { cleanup(emitter, "timed out") }
        emitter.onError { e -> cleanup(emitter, "error: ${e.message}") }

        logger.info("SSE connection opened. active={}", emitters.size)
        eventPublisher.publishEvent(AvatarSseSubscribedEvent(emitter))
        return emitter
    }

    @Async
    @EventListener
    @Transactional(readOnly = true)
    fun onAvatarSseSubscribed(event: AvatarSseSubscribedEvent) {
        send(event.emitter, findAvatarChanges())
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(readOnly = true)
    fun handleAvatarListChanged(event: AvatarListChangedEvent) {
        val avatarChanges = findAvatarChanges()
        emitters.forEach { emitter -> send(emitter, avatarChanges) }
    }

    @Scheduled(fixedDelayString = "\${aikon.sse.heartbeat-interval-millis:10000}")
    fun sendHeartbeat() {
        if (emitters.isEmpty()) return
        emitters.forEach { emitter ->
            var failed = false
            synchronized(emitter) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"))
                } catch (e: IOException) {
                    logger.warn("Heartbeat send failed {}", e.message)
                    failed = true
                } catch (e: IllegalStateException) {
                    logger.warn("Heartbeat send failed (closed emitter) {}", e.message)
                    failed = true
                }
            }
            if (failed) cleanup(emitter, "heartbeat failed")
        }
    }

    private fun findAvatarChanges(): List<AvatarChangeResDto> =
        avatarRepository
            .findAllByOrderByIdAsc()
            .map { avatar -> avatar.toChangeResDto() }

    private fun send(
        emitter: SseEmitter,
        avatarChanges: List<AvatarChangeResDto>,
    ) {
        var failed = false
        synchronized(emitter) {
            try {
                emitter.send(
                    SseEmitter
                        .event()
                        .name(EVENT_NAME)
                        .data(avatarChanges),
                )
            } catch (e: IOException) {
                logger.warn("Send failed, removing emitter {}", e.message)
                failed = true
            } catch (e: IllegalStateException) {
                logger.warn("Send failed (closed emitter) {}", e.message)
                failed = true
            }
        }
        if (failed) cleanup(emitter, "send failed")
    }

    private fun cleanup(
        emitter: SseEmitter,
        reason: String,
    ) {
        if (emitters.remove(emitter)) {
            logger.info("SSE connection {} active={}", reason, emitters.size)
        }
    }

    private fun AvatarEntity.toChangeResDto(): AvatarChangeResDto =
        AvatarChangeResDto(
            id = id,
            nickname = nickname,
            style = style,
            gender = gender,
            ageRange = ageRange,
            generationStatus = generationStatus,
            imageUrl = imageUrl,
            passUrl = passUrl,
            createdAt = createdAt,
        )

    companion object {
        private const val EVENT_NAME = "avatar-list-changed"
    }
}
