package team.darkmoderap.aikon.domain.avatar.event

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import team.darkmoderap.aikon.domain.avatar.service.GenerateAvatarImageService

@Component
class AvatarCreatedEventListener(
    private val generateAvatarImageService: GenerateAvatarImageService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handle(event: AvatarCreatedEvent) {
        generateAvatarImageService.execute(event.avatarId, event.sourceImage)
    }
}
