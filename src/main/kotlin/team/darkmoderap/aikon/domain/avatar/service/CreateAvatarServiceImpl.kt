package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarResDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.event.AvatarImageGenerationRequestedEvent
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class CreateAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : CreateAvatarService {
    @Transactional
    override fun execute(reqDto: CreateAvatarReqDto): CreateAvatarResDto {
        val passCode = allocatePassCode()
        val avatar =
            avatarRepository.save(
                AvatarEntity(
                    nickname = reqDto.nickname!!,
                    gender = reqDto.gender!!,
                    style = reqDto.style!!,
                    ageRange = reqDto.ageRange!!,
                    passUrl = passCode,
                ),
            )

        eventPublisher.publishEvent(AvatarImageGenerationRequestedEvent(avatar.id))
        eventPublisher.publishEvent(AvatarListChangedEvent())

        return CreateAvatarResDto(
            id = avatar.id,
            generationStatus = avatar.generationStatus,
        )
    }

    private fun allocatePassCode(): String {
        val passCodes = PASS_CODE_RANGE.map { "$PASS_CODE_PREFIX$it" }
        val usedPassCodes = avatarRepository.findAllByPassUrlIn(passCodes).mapNotNull { it.passUrl }.toSet()

        return passCodes.firstOrNull { it !in usedPassCodes }
            ?: throw AikonException(ErrorCode.AVATAR_PASS_CODE_EXHAUSTED)
    }

    companion object {
        private const val PASS_CODE_PREFIX = "Aikon"
        private val PASS_CODE_RANGE = 500..899
    }
}
