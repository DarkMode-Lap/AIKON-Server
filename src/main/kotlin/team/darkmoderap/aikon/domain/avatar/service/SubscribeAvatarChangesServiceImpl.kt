package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.darkmoderap.aikon.domain.avatar.dto.AvatarChangeResDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

@Service
class SubscribeAvatarChangesServiceImpl(
    private val avatarRepository: AvatarRepository,
) : SubscribeAvatarChangesService {
    private val logger = LoggerFactory.getLogger(SubscribeAvatarChangesServiceImpl::class.java)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    @Transactional(readOnly = true)
    override fun execute(): SseEmitter {
        val emitter = SseEmitter(TIMEOUT_MILLIS)
        emitters.add(emitter)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        send(emitter, findAvatarChanges())

        return emitter
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleAvatarListChanged(event: AvatarListChangedEvent) {
        val avatarChanges = findAvatarChanges()
        emitters.forEach { emitter -> send(emitter, avatarChanges) }
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
                emitters.remove(emitter)
            } catch (exception: IllegalStateException) {
                logger.warn("Removed closed avatar change emitter {}", exception.message)
                emitters.remove(emitter)
            }
        }
    }

    private fun AvatarEntity.toChangeResDto(): AvatarChangeResDto =
        AvatarChangeResDto(
            nickname = nickname,
            style = style,
            gender = gender,
            ageRange = ageRange,
            generationStatus = generationStatus,
        )

    companion object {
        private const val EVENT_NAME = "avatar-list-changed"
        private const val TIMEOUT_MILLIS = 30L * 60L * 1000L
    }
}
