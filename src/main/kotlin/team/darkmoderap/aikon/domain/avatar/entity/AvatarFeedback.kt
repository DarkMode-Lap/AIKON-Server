package team.darkmoderap.aikon.domain.avatar.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.darkmoderap.aikon.domain.avatar.entity.enum.FeedbackRating
import team.darkmoderap.aikon.domain.avatar.entity.enum.FeedbackReason
import team.darkmoderap.aikon.global.common.entity.BaseEntity

@Entity
@Table(
    name = "avatar_feedbacks",
    uniqueConstraints = [UniqueConstraint(columnNames = ["avatar_id"])],
)
class AvatarFeedback(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    val avatar: AvatarEntity,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var rating: FeedbackRating,
    @ElementCollection
    @CollectionTable(name = "avatar_feedback_reasons", joinColumns = [JoinColumn(name = "feedback_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    var reasons: List<FeedbackReason>,
    @Column(length = 500)
    var comment: String? = null,
    @Column(name = "training_consent", nullable = false)
    var trainingConsent: Boolean = false,
    @Column(name = "feedback_use_consent", nullable = false)
    var feedbackUseConsent: Boolean = false,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseEntity() {
    fun update(
        rating: FeedbackRating,
        reasons: List<FeedbackReason>,
        comment: String?,
        trainingConsent: Boolean,
        feedbackUseConsent: Boolean,
    ) {
        this.rating = rating
        this.reasons = reasons
        this.comment = comment
        this.trainingConsent = trainingConsent
        this.feedbackUseConsent = feedbackUseConsent
    }
}
