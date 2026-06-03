package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class DeleteAllAvatarsServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageStorage: AvatarImageStorage,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteAllAvatarsService {
    @Transactional
    override fun execute() {
        val avatars = avatarRepository.findAllByOrderByIdAsc()

        avatars.forEach { avatar -> deleteImageIfExists(avatar.imageUrl) }
        avatarRepository.deleteAllInBatch()
        eventPublisher.publishEvent(AvatarListChangedEvent())
    }

    private fun deleteImageIfExists(imageUrl: String?) {
        if (imageUrl == null) return

        try {
            avatarImageStorage.delete(imageUrl)
        } catch (exception: Exception) {
            throw AikonException(ErrorCode.AVATAR_IMAGE_DELETE_FAILED, cause = exception)
        }
    }
}
