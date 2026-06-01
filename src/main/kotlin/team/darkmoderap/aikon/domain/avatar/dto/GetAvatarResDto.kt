package team.darkmoderap.aikon.domain.avatar.dto

data class GetAvatarResDto(
    val id: Long,
    val nickname: String,
    val imageUrl: String?,
    val passUrl: String?,
)
