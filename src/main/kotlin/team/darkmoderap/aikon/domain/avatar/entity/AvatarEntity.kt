package team.darkmoderap.aikon.domain.avatar.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.global.common.entity.BaseEntity

@Entity
@Table(name = "avatars")
class AvatarEntity(
    @Column(nullable = false, length = 50)
    var nickname: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var style: Style,
    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", nullable = false)
    var ageRange: AgeRange,
    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false)
    var generationStatus: GenerationStatus = GenerationStatus.WAITING,
    @Column(name = "image_url")
    var imageUrl: String? = null,
    @Column(name = "pass_url")
    var passUrl: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseEntity() {
    fun update(
        nickname: String,
        gender: Gender,
        ageRange: AgeRange,
    ) {
        this.nickname = nickname
        this.gender = gender
        this.ageRange = ageRange
    }
}
