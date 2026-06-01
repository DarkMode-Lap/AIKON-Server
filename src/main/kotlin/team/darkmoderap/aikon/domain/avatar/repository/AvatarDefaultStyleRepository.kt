package team.darkmoderap.aikon.domain.avatar.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.darkmoderap.aikon.domain.avatar.entity.AvatarDefaultStyle

interface AvatarDefaultStyleRepository : JpaRepository<AvatarDefaultStyle, Long>
