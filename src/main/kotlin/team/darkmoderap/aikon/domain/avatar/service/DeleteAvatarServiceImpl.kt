package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class DeleteAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
) : DeleteAvatarService {
    @Transactional
    override fun execute(avatarId: Long) {
        if (!avatarRepository.existsById(avatarId)) {
            throw AikonException(ErrorCode.AVATAR_NOT_FOUND)
        }
        avatarRepository.deleteById(avatarId)
    }
}
