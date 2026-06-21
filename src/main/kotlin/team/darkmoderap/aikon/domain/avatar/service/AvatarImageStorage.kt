package team.darkmoderap.aikon.domain.avatar.service

interface AvatarImageStorage {
    fun upload(
        avatarId: Long,
        image: GeneratedAvatarImage,
    ): String

    fun uploadSourceImage(
        avatarId: Long,
        bytes: ByteArray,
        mimeType: String,
    ): String

    fun toPublicUrl(s3Uri: String): String

    fun delete(imageUrl: String)
}
