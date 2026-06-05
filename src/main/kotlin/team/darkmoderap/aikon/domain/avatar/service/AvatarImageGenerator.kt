package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style

interface AvatarImageGenerator {
    fun generate(command: AvatarImageGenerationCommand): GeneratedAvatarImage
}

data class AvatarImageGenerationCommand(
    val style: Style,
    val gender: Gender,
    val ageRange: AgeRange,
    val sourceImage: ByteArray,
    val sourceMimeType: String,
)

data class GeneratedAvatarImage(
    val bytes: ByteArray,
    val mimeType: String,
)
