package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.AvatarGenerationCallbackReqDto
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.time.Instant

@Service
class HandleAvatarGenerationCallbackServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageStorage: AvatarImageStorage,
    private val eventPublisher: ApplicationEventPublisher,
) : HandleAvatarGenerationCallbackService {
    @Transactional
    override fun execute(reqDto: AvatarGenerationCallbackReqDto) {
        val avatar =
            avatarRepository.findByIdOrNull(reqDto.avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        if (reqDto.jobId != avatar.aiJobId) {
            throw AikonException(ErrorCode.INVALID_AVATAR_JOB_ID)
        }

        val completedAt = Instant.now()

        if (reqDto.status == "COMPLETED" && reqDto.generatedImageUri != null) {
            val imageUrl = avatarImageStorage.toPublicUrl(reqDto.generatedImageUri)
            avatar.completeGeneration(
                imageUrl = imageUrl,
                modelName = reqDto.modelName,
                promptVersion = reqDto.promptVersion,
                promptText = reqDto.promptText,
                durationMs = reqDto.durationMs,
                completedAt = completedAt,
            )
        } else {
            avatar.failGeneration(
                errorCode = reqDto.errorCode,
                completedAt = completedAt,
            )
        }

        eventPublisher.publishEvent(AvatarListChangedEvent())
    }
}
