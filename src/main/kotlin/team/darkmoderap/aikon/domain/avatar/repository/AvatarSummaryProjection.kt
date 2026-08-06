package team.darkmoderap.aikon.domain.avatar.repository

import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import java.time.Instant

interface AvatarSummaryProjection {
    val id: Long
    val nickname: String
    val style: Style
    val gender: Gender
    val ageRange: AgeRange
    val generationStatus: GenerationStatus
    val imageUrl: String?
    val passUrl: String?
    val createdAt: Instant
}
