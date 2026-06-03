package team.darkmoderap.aikon.domain.avatar.dto

import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus

data class GetAvatarResDto(
    val id: Long,
    val nickname: String,
    val generationStatus: GenerationStatus,
    val imageUrl: String?,
    val passUrl: String?,
    val qrUrl: String?,
)
