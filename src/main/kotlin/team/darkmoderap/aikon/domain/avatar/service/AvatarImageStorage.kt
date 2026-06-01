package team.darkmoderap.aikon.domain.avatar.service

interface AvatarImageStorage {
    fun upload(
        avatarId: Long,
        image: GeneratedAvatarImage,
    ): String

    fun delete(imageUrl: String)
}
