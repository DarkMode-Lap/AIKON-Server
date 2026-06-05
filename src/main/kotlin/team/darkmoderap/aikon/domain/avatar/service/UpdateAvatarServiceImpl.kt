package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class UpdateAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : UpdateAvatarService {
    @Transactional
    override fun execute(
        avatarId: Long,
        reqDto: UpdateAvatarReqDto,
    ) {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        if (avatar.generationStatus.isGenerating()) {
            throw AikonException(ErrorCode.AVATAR_GENERATION_IN_PROGRESS)
        }

        val nickname = reqDto.nickname ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val gender = reqDto.gender ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val ageRange = reqDto.ageRange ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)

        avatar.update(nickname, gender, ageRange)
        eventPublisher.publishEvent(AvatarListChangedEvent())
    }
}
