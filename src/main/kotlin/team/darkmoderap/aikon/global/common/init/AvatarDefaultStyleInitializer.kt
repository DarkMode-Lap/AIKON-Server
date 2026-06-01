package team.darkmoderap.aikon.global.common.init

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import team.darkmoderap.aikon.domain.avatar.entity.AvatarDefaultStyle
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.repository.AvatarDefaultStyleRepository

@Component
class AvatarDefaultStyleInitializer(
    private val avatarDefaultStyleRepository: AvatarDefaultStyleRepository,
) : CommandLineRunner {
    @Transactional
    override fun run(vararg args: String) {
        if (!avatarDefaultStyleRepository.existsById(AvatarDefaultStyle.SINGLETON_ID)) {
            avatarDefaultStyleRepository.save(AvatarDefaultStyle(defaultStyle = Style.STUDIO))
        }
    }
}
