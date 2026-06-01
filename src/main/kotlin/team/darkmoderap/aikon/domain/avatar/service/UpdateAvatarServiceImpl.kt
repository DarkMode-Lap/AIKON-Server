package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.repository.AvatarRepository

@Service
class UpdateAvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
) : UpdateAvatarService {
    // TODO: feat/global-exception-handler 머지 후 AikonException(ErrorCode.AVATAR_*)으로 교체
    @Transactional
    override fun execute(
        avatarId: Long,
        reqDto: UpdateAvatarReqDto,
    ) {
        val avatar =
            avatarRepository.findByIdOrNull(avatarId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "아바타를 찾을 수 없습니다.")

        if (avatar.generationStatus.isGenerating()) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "아바타가 생성 중이므로 수정할 수 없습니다.")
        }

        avatar.update(reqDto.nickname, reqDto.gender, reqDto.ageRange)
    }
}
