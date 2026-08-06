package team.darkmoderap.aikon.domain.avatar.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.darkmoderap.aikon.domain.avatar.entity.AvatarFeedback

interface AvatarFeedbackRepository : JpaRepository<AvatarFeedback, Long> {
    fun findByAvatarId(avatarId: Long): AvatarFeedback?
}
