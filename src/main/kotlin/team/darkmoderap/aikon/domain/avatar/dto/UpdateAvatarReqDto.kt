package team.darkmoderap.aikon.domain.avatar.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender

data class UpdateAvatarReqDto(
    @field:NotBlank
    @field:Size(max = 50)
    val nickname: String,
    val gender: Gender,
    val ageRange: AgeRange,
)
