package team.darkmoderap.aikon.domain.avatar.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender

data class UpdateAvatarReqDto(
    @field:NotBlank
    @field:Size(max = 50)
    val nickname: String?,
    @field:NotNull
    val gender: Gender?,
    @field:NotNull
    val ageRange: AgeRange?,
)
