package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

@Service
class SubscribeAvatarChangesServiceImpl(
    private val avatarRepository: AvatarRepository,
    @Value("\${aikon.sse.timeout-millis:1800000}") private val timeoutMillis: Long,
    @Value("\${aikon.sse.max-connections:100}") private val maxConnections: Int,
) : SubscribeAvatarChangesService {
    private val logger = LoggerFactory.getLogger(SubscribeAvatarChangesServiceImpl::class.java)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()
    private val activeConnections = AtomicInteger(0)

    @Transactional(readOnly = true)
    override fun execute(): SseEmitter {
        if (activeConnections.incrementAndGet() > maxConnections) {
            activeConnections.decrementAndGet()
            throw AikonException(ErrorCode.SSE_MAX_CONNECTIONS_EXCEEDED)
        }
        val emitter = SseEmitter(timeoutMillis)
        try {
            emitters.add(emitter)

            emitter.onCompletion {
                if (emitters.remove(emitter)) {
                    logger.info("SSE connection closed. active={}", activeConnections.decrementAndGet())
                }
            }
            emitter.onTimeout {
                if (emitters.remove(emitter)) {
                    logger.info("SSE connection timed out. active={}", activeConnections.decrementAndGet())
                }
            }
            emitter.onError { e ->
                if (emitters.remove(emitter)) {
                    logger.warn("SSE connection error {}. active={}", e.message, activeConnections.decrementAndGet())
                }
            }

            logger.info("SSE connection opened. active={}", activeConnections.get())
            send(emitter, findAvatarChanges())
        } catch (e: Throwable) {
            if (emitters.remove(emitter)) activeConnections.decrementAndGet()
            throw e
        }

        return emitter
    }

    @Async
    @Transactional(readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleAvatarListChanged(event: AvatarListChangedEvent) {
        val avatarChanges = findAvatarChanges()
        emitters.forEach { emitter -> send(emitter, avatarChanges) }
    }

    @Scheduled(fixedDelayString = "\${aikon.sse.heartbeat-interval-millis:30000}")
    fun sendHeartbeat() {
        if (emitters.isEmpty()) return
        val deadEmitters = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            synchronized(emitter) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"))
                } catch (exception: IOException) {
                    logger.warn("Removed failed emitter on heartbeat {}", exception.message)
                    deadEmitters.add(emitter)
                } catch (exception: IllegalStateException) {
                    logger.warn("Removed closed emitter on heartbeat {}", exception.message)
                    deadEmitters.add(emitter)
                }
            }
        }
        if (deadEmitters.isNotEmpty()) {
            var removedCount = 0
            deadEmitters.forEach { dead ->
                if (emitters.remove(dead)) removedCount++
            }
            if (removedCount > 0) activeConnections.addAndGet(-removedCount)
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
        synchronized(emitter) {
            try {
                emitter.send(
                    SseEmitter
                        .event()
                        .name(EVENT_NAME)
                        .data(avatarChanges),
                )
            } catch (exception: IOException) {
                logger.warn("Removed failed avatar change emitter {}", exception.message)
                if (emitters.remove(emitter)) activeConnections.decrementAndGet()
            } catch (exception: IllegalStateException) {
                logger.warn("Removed closed avatar change emitter {}", exception.message)
                if (emitters.remove(emitter)) activeConnections.decrementAndGet()
            }
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
