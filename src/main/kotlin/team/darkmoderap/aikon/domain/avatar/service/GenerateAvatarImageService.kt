package team.darkmoderap.aikon.domain.avatar.service

interface GenerateAvatarImageService {
    fun execute(
        avatarId: Long,
        sourceImage: AvatarSourceImage,
    )
}
