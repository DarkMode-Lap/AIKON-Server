package team.darkmoderap.aikon.domain.avatar.dto

import io.swagger.v3.oas.annotations.media.Schema
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus

@Schema(description = "아바타 생성 응답")
data class CreateAvatarResDto(
    @Schema(description = "아바타 ID", example = "1")
    val id: Long,
    @Schema(description = "생성 상태. 생성 직후에는 항상 WAITING")
    val generationStatus: GenerationStatus,
)
