package team.darkmoderap.aikon.domain.avatar.service

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import team.darkmoderap.aikon.domain.avatar.dto.FastApiGenerationReqDto
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.client.FastApiClient
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.time.Instant

@Service
class GenerateAvatarImageServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageStorage: AvatarImageStorage,
    private val fastApiClient: FastApiClient,
    private val eventPublisher: ApplicationEventPublisher,
    private val transactionTemplate: TransactionTemplate,
    @Value("\${aikon.api-base-url}") private val apiBaseUrl: String,
) : GenerateAvatarImageService {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun validateApiBaseUrl() {
        require(apiBaseUrl.isNotBlank() && (apiBaseUrl.startsWith("http://") || apiBaseUrl.startsWith("https://"))) {
            "aikon.api-base-url must be an absolute HTTP/HTTPS URL"
        }
    }

    @Async
    override fun execute(
        avatarId: Long,
        sourceImage: AvatarSourceImage,
    ) {
        val avatar =
            transactionTemplate.execute {
                avatarRepository.findByIdOrNull(avatarId)
            } ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        val sourceImageUri =
            runCatching {
                avatarImageStorage.uploadSourceImage(avatarId, sourceImage.bytes, sourceImage.mimeType)
            }.getOrElse { exception ->
                logger.error("Failed to upload source image for avatar {}", avatarId, exception)
                transactionTemplate.executeWithoutResult {
                    avatarRepository.findByIdOrNull(avatarId)?.failGeneration(null, Instant.now())
                }
                eventPublisher.publishEvent(AvatarListChangedEvent())
                return
            }

        transactionTemplate.executeWithoutResult {
            avatarRepository.findByIdOrNull(avatarId)?.let { it.sourceImageUri = sourceImageUri }
        }

        val callbackUrl = "$apiBaseUrl/internal/ai/avatar-generations/callback"

        val reqDto =
            FastApiGenerationReqDto(
                avatarId = avatarId,
                sourceImageUri = sourceImageUri,
                style = avatar.style.name,
                gender = avatar.gender.name,
                ageRange = avatar.ageRange.name,
                callbackUrl = callbackUrl,
            )

        runCatching {
            val res = fastApiClient.requestAvatarGeneration(reqDto)
            transactionTemplate.executeWithoutResult {
                avatarRepository.findByIdOrNull(avatarId)?.let { it.aiJobId = res.jobId }
            }
        }.onFailure { exception ->
            logger.error("Failed to request avatar generation to FastAPI for avatar {}", avatarId, exception)
            transactionTemplate.executeWithoutResult {
                avatarRepository.findByIdOrNull(avatarId)?.failGeneration(null, Instant.now())
            }
            eventPublisher.publishEvent(AvatarListChangedEvent())
        }
    }
}
