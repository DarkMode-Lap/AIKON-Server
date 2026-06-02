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
import team.darkmoderap.aikon.domain.avatar.event.AvatarListChangedEvent
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class CreateAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarImageGenerationTask: AvatarImageGenerationTask,
    private val eventPublisher: ApplicationEventPublisher,
) : CreateAvatarService {
    @Transactional
    override fun execute(
        reqDto: CreateAvatarReqDto,
        image: MultipartFile,
    ): CreateAvatarResDto {
        val sourceImage = image.toSourceImage()
        val passCode = allocatePassCode()
        val avatar =
            try {
                avatarRepository.saveAndFlush(
                    AvatarEntity(
                        nickname = requireNotNull(reqDto.nickname),
                        gender = requireNotNull(reqDto.gender),
                        style = requireNotNull(reqDto.style),
                        ageRange = requireNotNull(reqDto.ageRange),
                        generationStatus = GenerationStatus.PROCESSING,
                        passUrl = passCode,
                    ),
                )
            } catch (exception: DataIntegrityViolationException) {
                throw AikonException(ErrorCode.AVATAR_PASS_CODE_ASSIGNMENT_FAILED, cause = exception)
            }

        avatarImageGenerationTask.generate(
            avatarId = avatar.id,
            style = avatar.style,
            sourceBytes = sourceImage.bytes,
            sourceMimeType = sourceImage.mimeType,
        )
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

    private fun MultipartFile.toSourceImage(): SourceImage {
        if (isEmpty) {
            throw AikonException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val mimeType =
            contentType
                ?.takeIf { it.startsWith("image/") }
                ?: throw AikonException(ErrorCode.INVALID_INPUT_VALUE)

        return SourceImage(
            bytes = bytes,
            mimeType = mimeType,
        )
    }

    private data class SourceImage(
        val bytes: ByteArray,
        val mimeType: String,
    )

    companion object {
        private const val PASS_CODE_PREFIX = "Aikon"
        private val PASS_CODE_RANGE = 500..899
        private val ALL_PASS_CODES = PASS_CODE_RANGE.map { "$PASS_CODE_PREFIX$it" }
    }
}
