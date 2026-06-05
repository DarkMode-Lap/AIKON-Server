package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class DeleteAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageStorage: AvatarImageStorage,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteAvatarService {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(avatarId: Long) {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        if (avatar.generationStatus.isGenerating()) {
            throw AikonException(ErrorCode.AVATAR_GENERATION_IN_PROGRESS)
        }

        deleteImageIfExists(avatar.imageUrl)
        avatarRepository.delete(avatar)
        eventPublisher.publishEvent(AvatarListChangedEvent())
    }

    private fun deleteImageIfExists(imageUrl: String?) {
        if (imageUrl == null) {
            return
        }

        try {
            avatarImageStorage.delete(imageUrl)
        } catch (exception: Exception) {
            logger.warn("Failed to delete avatar image {} from storage", imageUrl, exception)
        }
    }
}
