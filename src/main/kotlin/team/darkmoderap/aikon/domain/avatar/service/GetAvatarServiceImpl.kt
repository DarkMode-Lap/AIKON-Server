package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class GetAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
) : GetAvatarService {
    @Transactional(readOnly = true)
    override fun execute(avatarId: Long): GetAvatarResDto {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        return GetAvatarResDto(
            id = avatar.id,
            nickname = avatar.nickname,
            imageUrl = avatar.imageUrl,
            passUrl = avatar.passUrl,
        )
    }
}
