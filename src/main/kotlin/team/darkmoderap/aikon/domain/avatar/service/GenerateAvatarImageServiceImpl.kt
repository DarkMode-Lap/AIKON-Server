package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class GenerateAvatarImageServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageGenerator: AvatarImageGenerator,
    private val avatarImageStorage: AvatarImageStorage,
    private val eventPublisher: ApplicationEventPublisher,
) : GenerateAvatarImageService {
    @Async
    @Transactional
    override fun execute(
        avatarId: Long,
        sourceImage: AvatarSourceImage,
    ) {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        try {
            val generatedImage =
                avatarImageGenerator.generate(
                    AvatarImageGenerationCommand(
                        style = avatar.style,
                        sourceImage = sourceImage.bytes,
                        sourceMimeType = sourceImage.mimeType,
                    ),
                )
            val imageUrl = avatarImageStorage.upload(avatar.id, generatedImage)
            avatar.completeGeneration(imageUrl)
        } catch (exception: Exception) {
            avatar.failGeneration()
        }

        eventPublisher.publishEvent(AvatarListChangedEvent())
    }
}
