package team.darkmoderap.aikon.domain.avatar.dto

import io.swagger.v3.oas.annotations.media.Schema
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style

@Schema(description = "기본 스타일 변경 요청")
data class UpdateDefaultStyleReqDto(
    @Schema(description = "스타일 (STUDIO, ZOOTOPIA, TRADITIONAL_HANBOK, DISNEY_PIXAR, GHIBLI, LIGHT_ART)")
    val style: Style,
)
