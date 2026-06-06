package team.darkmoderap.aikon.domain.avatar.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity

interface AvatarRepository : JpaRepository<AvatarEntity, Long> {
    fun findAllByOrderByIdAsc(): List<AvatarEntity>

    fun findByPassUrl(passUrl: String): AvatarEntity?

    @Query("select avatar.passUrl from AvatarEntity avatar where avatar.passUrl is not null")
    fun findAllPassUrls(): List<String>
}
