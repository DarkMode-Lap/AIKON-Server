package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository

@Service
class DeleteAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
) : DeleteAvatarService {
    // TODO: feat/global-exception-handler 머지 후 AikonException(ErrorCode.AVATAR_NOT_FOUND)으로 교체
    @Transactional
    override fun execute(avatarId: Long) {
        if (!avatarRepository.existsById(avatarId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "아바타를 찾을 수 없습니다.")
        }
        avatarRepository.deleteById(avatarId)
    }
}
