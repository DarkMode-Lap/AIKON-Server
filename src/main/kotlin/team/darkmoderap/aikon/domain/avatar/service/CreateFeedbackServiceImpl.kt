package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.darkmoderap.aikon.domain.avatar.dto.CreateFeedbackReqDto
import team.darkmoderap.aikon.domain.avatar.dto.FastApiFeedbackReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarFeedback
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.repository.AvatarFeedbackRepository
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.client.FastApiClient
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class CreateFeedbackServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarFeedbackRepository: AvatarFeedbackRepository,
    private val fastApiClient: FastApiClient,
) : CreateFeedbackService {
    @Transactional
    override fun execute(
        avatarId: Long,
        reqDto: CreateFeedbackReqDto,
    ) {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        if (avatar.generationStatus != GenerationStatus.COMPLETED || avatar.imageUrl == null) {
            throw AikonException(ErrorCode.AVATAR_NOT_COMPLETED)
        }

        val rating = reqDto.rating ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val reasons = reqDto.reasons ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val trainingConsent = reqDto.trainingConsent ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val feedbackUseConsent = reqDto.feedbackUseConsent ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)

        val existing = avatarFeedbackRepository.findByAvatarId(avatarId)
        if (existing != null) {
            existing.update(
                rating = rating,
                reasons = reasons,
                comment = reqDto.comment,
                trainingConsent = trainingConsent,
                feedbackUseConsent = feedbackUseConsent,
            )
        } else {
            avatarFeedbackRepository.save(
                AvatarFeedback(
                    avatar = avatar,
                    rating = rating,
                    reasons = reasons.toMutableList(),
                    comment = reqDto.comment,
                    trainingConsent = trainingConsent,
                    feedbackUseConsent = feedbackUseConsent,
                ),
            )
        }

        val fastApiReqDto =
            FastApiFeedbackReqDto(
                avatarId = avatarId,
                jobId = avatar.aiJobId,
                rating = rating.name,
                reasons = reasons.map { it.name },
                comment = reqDto.comment,
                trainingConsent = trainingConsent,
                feedbackUseConsent = feedbackUseConsent,
                style = avatar.style.name,
                gender = avatar.gender.name,
                ageRange = avatar.ageRange.name,
                promptVersion = avatar.promptVersion,
                modelName = avatar.modelName,
            )

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        fastApiClient.sendFeedback(fastApiReqDto)
                    }
                },
            )
        } else {
            fastApiClient.sendFeedback(fastApiReqDto)
        }
    }
}
