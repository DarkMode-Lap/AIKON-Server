package team.darkmoderap.aikon.domain.avatar.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity

interface AvatarRepository : JpaRepository<AvatarEntity, Long> {
    fun findAllByOrderByIdAsc(): List<AvatarEntity>

    fun findAllByPassUrlIn(passUrls: Collection<String>): List<AvatarEntity>
}
