package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class GetAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarQrUrlProvider: AvatarQrUrlProvider,
) : GetAvatarService {
    @Transactional(readOnly = true)
    override fun execute(avatarId: Long): GetAvatarResDto {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        val isCompleted = avatar.generationStatus == GenerationStatus.COMPLETED
        val imageUrl = if (isCompleted) avatar.imageUrl else null
        val passUrl = if (isCompleted) avatar.passUrl else null

        return GetAvatarResDto(
            id = avatar.id,
            nickname = avatar.nickname,
            generationStatus = avatar.generationStatus,
            imageUrl = imageUrl,
            passUrl = passUrl,
            qrUrl = passUrl?.let { avatarQrUrlProvider.create(it) },
        )
    }
}
