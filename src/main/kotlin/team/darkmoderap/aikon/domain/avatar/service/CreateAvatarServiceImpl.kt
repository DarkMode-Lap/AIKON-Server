package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarResDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.event.AvatarCreatedEvent
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
    override fun execute(
        reqDto: CreateAvatarReqDto,
        image: MultipartFile,
    ): CreateAvatarResDto {
        val sourceImage = image.toSourceImage()
        val passCode = allocatePassCode()
        val nickname = reqDto.nickname ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val gender = reqDto.gender ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val style = reqDto.style ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val ageRange = reqDto.ageRange ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        val avatar =
            try {
                avatarRepository.saveAndFlush(
                    AvatarEntity(
                        nickname = nickname,
                        gender = gender,
                        style = style,
                        ageRange = ageRange,
                        generationStatus = GenerationStatus.PROCESSING,
                        passUrl = passCode,
                    ),
                )
            } catch (exception: DataIntegrityViolationException) {
                throw AikonException(ErrorCode.AVATAR_PASS_CODE_ASSIGNMENT_FAILED, cause = exception)
            }

        eventPublisher.publishEvent(AvatarCreatedEvent(avatar.id, sourceImage))
        eventPublisher.publishEvent(AvatarListChangedEvent())

        return CreateAvatarResDto(
            id = avatar.id,
            generationStatus = avatar.generationStatus,
        )
    }

    private fun allocatePassCode(): String {
        val usedPassCodes = avatarRepository.findAllPassUrls().toSet()

        return ALL_PASS_CODES.firstOrNull { it !in usedPassCodes }
            ?: throw AikonException(ErrorCode.AVATAR_PASS_CODE_EXHAUSTED)
    }

    private fun MultipartFile.toSourceImage(): AvatarSourceImage {
        if (isEmpty) {
            throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val mimeType =
            contentType
                ?.takeIf { it.startsWith("image/") }
                ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)

        return AvatarSourceImage(
            bytes = bytes,
            mimeType = mimeType,
        )
    }

    companion object {
        private const val PASS_CODE_PREFIX = "Aikon"
        private val PASS_CODE_RANGE = 500..899
        private val ALL_PASS_CODES = PASS_CODE_RANGE.map { "$PASS_CODE_PREFIX$it" }
    }
}
