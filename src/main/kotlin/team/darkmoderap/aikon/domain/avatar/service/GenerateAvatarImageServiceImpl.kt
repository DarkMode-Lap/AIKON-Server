package team.darkmoderap.aikon.domain.avatar.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
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
    private val transactionTemplate: TransactionTemplate,
) : GenerateAvatarImageService {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    override fun execute(
        avatarId: Long,
        sourceImage: AvatarSourceImage,
    ) {
        val avatar =
            transactionTemplate.execute {
                avatarRepository.findByIdOrNull(avatarId)
            } ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        val result =
            runCatching {
                val generatedImage =
                    avatarImageGenerator.generate(
                        AvatarImageGenerationCommand(
                            style = avatar.style,
                            gender = avatar.gender,
                            ageRange = avatar.ageRange,
                            sourceImage = sourceImage.bytes,
                            sourceMimeType = sourceImage.mimeType,
                        ),
                    )
                avatarImageStorage.upload(avatarId, generatedImage)
            }

        transactionTemplate.executeWithoutResult {
            val avatar =
                avatarRepository.findByIdOrNull(avatarId)
                    ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

            result.fold(
                onSuccess = { imageUrl -> avatar.completeGeneration(imageUrl) },
                onFailure = { exception ->
                    logger.error("Failed to generate avatar image for avatar {}", avatarId, exception)
                    avatar.failGeneration()
                },
            )
        }

        eventPublisher.publishEvent(AvatarListChangedEvent())
    }
}
