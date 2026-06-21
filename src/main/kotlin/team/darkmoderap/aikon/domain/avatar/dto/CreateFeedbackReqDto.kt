package team.darkmoderap.aikon.domain.avatar.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import team.darkmoderap.aikon.domain.avatar.entity.enum.FeedbackRating
import team.darkmoderap.aikon.domain.avatar.entity.enum.FeedbackReason

data class CreateFeedbackReqDto(
    @field:NotNull
    val rating: FeedbackRating?,
    @field:NotEmpty
    val reasons: List<FeedbackReason>?,
    val comment: String?,
    @field:NotNull
    val trainingConsent: Boolean?,
    @field:NotNull
    val feedbackUseConsent: Boolean?,
)
