package team.darkmoderap.aikon.domain.avatar.dto

data class AvatarGenerationCallbackReqDto(
    val avatarId: Long,
    val jobId: String,
    val status: String,
    val generatedImageUri: String?,
    val modelName: String?,
    val promptVersion: String?,
    val promptText: String?,
    val durationMs: Long?,
    val errorCode: String?,
    val errorMessage: String?,
)
