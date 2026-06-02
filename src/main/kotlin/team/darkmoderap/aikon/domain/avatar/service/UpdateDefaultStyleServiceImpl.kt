package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarDefaultStyle
import team.darkmoderap.aikon.domain.avatar.repository.AvatarDefaultStyleRepository
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class UpdateDefaultStyleServiceImpl(
    private val avatarDefaultStyleRepository: AvatarDefaultStyleRepository,
) : UpdateDefaultStyleService {
    @Transactional
    override fun execute(reqDto: UpdateDefaultStyleReqDto) {
        val setting =
            avatarDefaultStyleRepository.findByIdOrNull(AvatarDefaultStyle.SINGLETON_ID)
                ?: throw AikonException(ErrorCode.INTERNAL_SERVER_ERROR)
        setting.updateStyle(reqDto.style)
    }
}
