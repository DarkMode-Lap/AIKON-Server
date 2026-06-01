package team.darkmoderap.aikon.domain.avatar.dto

import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style

data class AvatarChangeResDto(
    val nickname: String,
    val style: Style,
    val gender: Gender,
    val ageRange: AgeRange,
    val generationStatus: GenerationStatus,
)
