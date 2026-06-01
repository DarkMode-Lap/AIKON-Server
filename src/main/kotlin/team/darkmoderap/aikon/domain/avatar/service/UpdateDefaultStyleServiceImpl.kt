package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.entity.AvatarDefaultStyle
import team.darkmoderap.aikon.domain.avatar.repository.AvatarDefaultStyleRepository

@Service
class UpdateDefaultStyleServiceImpl(
    private val avatarDefaultStyleRepository: AvatarDefaultStyleRepository,
) : UpdateDefaultStyleService {
    @Transactional
    override fun execute(reqDto: UpdateDefaultStyleReqDto) {
        val setting = avatarDefaultStyleRepository.findByIdOrNull(AvatarDefaultStyle.SINGLETON_ID)
        if (setting == null) {
            avatarDefaultStyleRepository.save(AvatarDefaultStyle(defaultStyle = reqDto.style))
            return
        }
        setting.updateStyle(reqDto.style)
    }
}
