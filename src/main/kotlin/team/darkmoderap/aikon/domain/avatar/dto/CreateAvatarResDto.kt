package team.darkmoderap.aikon.domain.avatar.dto

import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus

data class CreateAvatarResDto(
    val id: Long,
    val generationStatus: GenerationStatus,
)
