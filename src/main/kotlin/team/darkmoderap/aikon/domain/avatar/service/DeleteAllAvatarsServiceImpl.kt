package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository

@Service
class DeleteAllAvatarsServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageStorage: AvatarImageStorage,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteAllAvatarsService {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute() {
        val imageUrls = avatarRepository.findAllByOrderByIdAsc().mapNotNull { it.imageUrl }

        avatarRepository.deleteAllInBatch()

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    imageUrls.forEach { imageUrl ->
                        try {
                            avatarImageStorage.delete(imageUrl)
                        } catch (exception: Exception) {
                            logger.warn("Failed to delete avatar image {}", imageUrl, exception)
                        }
                    }
                    eventPublisher.publishEvent(AvatarListChangedEvent())
                }
            },
        )
    }
}
