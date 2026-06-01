package team.darkmoderap.aikon.domain.avatar.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.darkmoderap.aikon.domain.avatar.entity.AvatarEntity

interface AvatarRepository : JpaRepository<AvatarEntity, Long> {
    fun findAllByOrderByIdAsc(): List<AvatarEntity>

    @Query("select avatar.passUrl from AvatarEntity avatar where avatar.passUrl in :passUrls")
    fun findPassUrlsByPassUrlIn(
        @Param("passUrls") passUrls: Collection<String>,
    ): List<String>
}
