package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class UpdateAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
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

        avatar.update(reqDto.nickname!!, reqDto.gender!!, reqDto.ageRange!!)
    }
}
