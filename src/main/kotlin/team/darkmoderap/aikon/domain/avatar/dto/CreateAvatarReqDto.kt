package team.darkmoderap.aikon.domain.avatar.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style

@Schema(description = "아바타 생성 요청")
data class CreateAvatarReqDto(
    @Schema(description = "닉네임 (최대 50자)", example = "홍길동")
    @field:NotBlank
    @field:Size(max = 50)
    val nickname: String?,
    @Schema(description = "성별 (MALE, FEMALE)")
    @field:NotNull
    val gender: Gender?,
    @Schema(description = Style.SCHEMA_DESCRIPTION)
    @field:NotNull
    val style: Style?,
    @Schema(description = "연령대 (AGE_0_7, AGE_8_13, AGE_14_19, AGE_20_PLUS)")
    @field:NotNull
    val ageRange: AgeRange?,
)
