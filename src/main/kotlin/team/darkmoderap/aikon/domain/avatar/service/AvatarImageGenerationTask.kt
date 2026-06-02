package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository

@Component
class AvatarImageGenerationTask(
    private val avatarRepository: AvatarRepository,
    private val avatarImageGenerator: AvatarImageGenerator,
    private val avatarImageStorage: AvatarImageStorage,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @Transactional
    fun generate(
        avatarId: Long,
        style: Style,
        sourceBytes: ByteArray,
        sourceMimeType: String,
    ) {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: run {
                    logger.error("Avatar {} not found for image generation", avatarId)
                    return
                }

        try {
            val generatedImage =
                avatarImageGenerator.generate(
                    AvatarImageGenerationCommand(
                        style = style,
                        sourceImage = sourceBytes,
                        sourceMimeType = sourceMimeType,
                    ),
                )
            val imageUrl = avatarImageStorage.upload(avatarId, generatedImage)
            avatar.completeGeneration(imageUrl)
        } catch (exception: Exception) {
            logger.error("Failed to generate avatar image for avatar {}", avatarId, exception)
            avatar.failGeneration()
        }

        eventPublisher.publishEvent(AvatarListChangedEvent())
    }
}
