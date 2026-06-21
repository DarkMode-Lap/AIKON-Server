package team.darkmoderap.aikon.domain.avatar.dto

data class FastApiGenerationReqDto(
    val avatarId: Long,
    val sourceImageUri: String,
    val style: String,
    val gender: String,
    val ageRange: String,
    val callbackUrl: String,
)
