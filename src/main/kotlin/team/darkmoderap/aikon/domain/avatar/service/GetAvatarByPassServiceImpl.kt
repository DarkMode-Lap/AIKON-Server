package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class GetAvatarByPassServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val avatarQrUrlProvider: AvatarQrUrlProvider,
) : GetAvatarByPassService {
    @Transactional(readOnly = true)
    override fun execute(passUrl: String): GetAvatarResDto {
        val avatar =
            avatarRepository.findByPassUrl(passUrl)
                ?: throw AikonException(ErrorCode.AVATAR_NOT_FOUND)

        val isCompleted = avatar.generationStatus == GenerationStatus.COMPLETED
        val imageUrl = if (isCompleted) avatar.imageUrl else null
        val resolvedPassUrl = if (isCompleted) avatar.passUrl else null

        return GetAvatarResDto(
            id = avatar.id,
            nickname = avatar.nickname,
            generationStatus = avatar.generationStatus,
            imageUrl = imageUrl,
            passUrl = resolvedPassUrl,
            qrUrl = resolvedPassUrl?.let { avatarQrUrlProvider.create(it) },
        )
    }
}
