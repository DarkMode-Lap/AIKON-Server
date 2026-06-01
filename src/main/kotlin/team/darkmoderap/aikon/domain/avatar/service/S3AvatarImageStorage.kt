package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@Service
class S3AvatarImageStorage(
    private val s3Client: S3Client,
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.s3.bucket}") private val bucket: String,
    @Value("\${aws.s3.public-base-url}") private val publicBaseUrl: String,
) : AvatarImageStorage {
    override fun upload(
        avatarId: Long,
        image: GeneratedAvatarImage,
    ): String {
        if (bucket.isBlank()) {
            throw AikonException(ErrorCode.AVATAR_IMAGE_GENERATION_FAILED)
        }

        val key = "avatars/$avatarId.${image.mimeType.toExtension()}"
        val request =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .contentType(image.mimeType)
                .build()

        s3Client.putObject(request, RequestBody.fromBytes(image.bytes))

        return if (publicBaseUrl.isBlank()) {
            "https://$bucket.s3.$region.amazonaws.com/$key"
        } else {
            "${publicBaseUrl.trimEnd('/')}/$key"
        }
    }

    override fun delete(imageUrl: String) {
        if (bucket.isBlank()) {
            throw AikonException(ErrorCode.AVATAR_IMAGE_DELETE_FAILED)
        }

        val key =
            extractKey(imageUrl)
                ?: throw AikonException(ErrorCode.AVATAR_IMAGE_DELETE_FAILED)
        val request =
            DeleteObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .build()

        s3Client.deleteObject(request)
    }

    private fun extractKey(imageUrl: String): String? {
        val normalizedPublicBaseUrl = publicBaseUrl.trimEnd('/')
        if (normalizedPublicBaseUrl.isNotBlank() && imageUrl.startsWith("$normalizedPublicBaseUrl/")) {
            return imageUrl.removePrefix("$normalizedPublicBaseUrl/")
        }

        val awsPrefix = "https://$bucket.s3.$region.amazonaws.com/"
        if (imageUrl.startsWith(awsPrefix)) {
            return imageUrl.removePrefix(awsPrefix)
        }

        return null
    }

    private fun String.toExtension(): String =
        when (this.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            else -> "png"
        }
}
