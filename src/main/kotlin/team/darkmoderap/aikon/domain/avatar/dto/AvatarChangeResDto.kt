package team.darkmoderap.aikon.domain.avatar.dto

import io.swagger.v3.oas.annotations.media.Schema
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import java.time.Instant

@Schema(description = "SSE 아바타 목록 변경 이벤트 페이로드")
data class AvatarChangeResDto(
    @Schema(description = "아바타 ID", example = "1")
    val id: Long,
    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,
    @Schema(description = "스타일 (STUDIO, ZOOTOPIA, TRADITIONAL_HANBOK, DISNEY_PIXAR, GHIBLI, LIGHT_ART)")
    val style: Style,
    @Schema(description = "성별 (MALE, FEMALE)")
    val gender: Gender,
    @Schema(description = "연령대 (AGE_0_7, AGE_8_13, AGE_14_19, AGE_20_PLUS)")
    val ageRange: AgeRange,
    @Schema(description = "생성 상태 (WAITING, PROCESSING, COMPLETED, FAILED, RETRYING)")
    val generationStatus: GenerationStatus,
    @Schema(description = "아바타 이미지 URL. 생성 완료 전에는 null", nullable = true)
    val imageUrl: String?,
    @Schema(description = "패스 URL. 미발급 시 null", nullable = true)
    val passUrl: String?,
    @Schema(description = "생성 일시")
    val createdAt: Instant,
)
