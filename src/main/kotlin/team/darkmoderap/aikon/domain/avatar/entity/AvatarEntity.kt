package team.darkmoderap.aikon.domain.avatar.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.global.common.entity.BaseEntity
import java.time.Instant

@Entity
@Table(name = "avatars")
class AvatarEntity(
    @Column(nullable = false, length = 50)
    var nickname: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var gender: Gender,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var style: Style,
    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", nullable = false)
    var ageRange: AgeRange,
    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false)
    var generationStatus: GenerationStatus = GenerationStatus.WAITING,
    @Column(name = "image_url")
    var imageUrl: String? = null,
    @Column(name = "pass_url", unique = true)
    var passUrl: String? = null,
    @Column(name = "ai_job_id", length = 36)
    var aiJobId: String? = null,
    @Column(name = "source_image_uri", length = 500)
    var sourceImageUri: String? = null,
    @Column(name = "model_name", length = 100)
    var modelName: String? = null,
    @Column(name = "prompt_version", length = 20)
    var promptVersion: String? = null,
    @Column(name = "prompt_text", columnDefinition = "TEXT")
    var promptText: String? = null,
    @Column(name = "duration_ms")
    var durationMs: Long? = null,
    @Column(name = "error_code", length = 50)
    var errorCode: String? = null,
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseEntity() {
    fun update(
        nickname: String,
        gender: Gender,
        ageRange: AgeRange,
    ) {
        this.nickname = nickname
        this.gender = gender
        this.ageRange = ageRange
    }

    fun completeGeneration(
        imageUrl: String,
        modelName: String?,
        promptVersion: String?,
        promptText: String?,
        durationMs: Long?,
        completedAt: Instant?,
    ) {
        this.imageUrl = imageUrl
        this.generationStatus = GenerationStatus.COMPLETED
        this.modelName = modelName
        this.promptVersion = promptVersion
        this.promptText = promptText
        this.durationMs = durationMs
        this.completedAt = completedAt
    }

    fun failGeneration(
        errorCode: String?,
        completedAt: Instant?,
    ) {
        this.generationStatus = GenerationStatus.FAILED
        this.errorCode = errorCode
        this.completedAt = completedAt
    }
}
