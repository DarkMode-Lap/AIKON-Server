package team.darkmoderap.aikon.domain.avatar.dto

data class FastApiFeedbackReqDto(
    val avatarId: Long,
    val jobId: String?,
    val rating: String,
    val reasons: List<String>,
    val comment: String?,
    val trainingConsent: Boolean,
    val feedbackUseConsent: Boolean,
    val style: String,
    val gender: String,
    val ageRange: String,
    val promptVersion: String?,
    val modelName: String?,
)
