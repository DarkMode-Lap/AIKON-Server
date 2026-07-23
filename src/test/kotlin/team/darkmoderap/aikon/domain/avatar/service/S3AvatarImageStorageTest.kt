package team.darkmoderap.aikon.domain.avatar.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.net.URI

@ExtendWith(MockitoExtension::class)
class S3AvatarImageStorageTest {
    @Mock
    private lateinit var s3Client: S3Client

    @Mock
    private lateinit var s3Presigner: S3Presigner

    @Nested
    @DisplayName("upload 메서드는")
    inner class Upload {
        @Test
        @DisplayName("publicBaseUrl 기반 이미지 URL을 반환한다")
        fun `returns public base url image url`() {
            // Given
            val storage = s3AvatarImageStorage(publicBaseUrl = PUBLIC_BASE_URL)
            val image = GeneratedAvatarImage(byteArrayOf(1, 2, 3), "image/png")

            // When
            val result = storage.upload(AVATAR_ID, image)

            // Then
            assertEquals("$PUBLIC_BASE_URL/avatars/$AVATAR_ID.png", result)
        }

        @Test
        @DisplayName("이미지 mimeType에 맞는 확장자로 업로드한다")
        fun `uploads image with mime type extension`() {
            // Given
            val storage = s3AvatarImageStorage(publicBaseUrl = PUBLIC_BASE_URL)
            val image = GeneratedAvatarImage(byteArrayOf(1, 2, 3), "image/webp")

            // When
            storage.upload(AVATAR_ID, image)

            // Then
            val requestCaptor = ArgumentCaptor.forClass(PutObjectRequest::class.java)
            org.mockito.Mockito
                .verify(
                    s3Client,
                ).putObject(requestCaptor.capture(), org.mockito.ArgumentMatchers.any(RequestBody::class.java))
            assertEquals("avatars/$AVATAR_ID.webp", requestCaptor.value.key())
        }
    }

    @Nested
    @DisplayName("delete 메서드는")
    inner class Delete {
        @Test
        @DisplayName("publicBaseUrl 기반 URL에서 key를 추출해 삭제한다")
        fun `deletes object by extracted key from public base url`() {
            // Given
            val storage = s3AvatarImageStorage(publicBaseUrl = PUBLIC_BASE_URL)
            val imageUrl = "$PUBLIC_BASE_URL/avatars/$AVATAR_ID.png"

            // When
            storage.delete(imageUrl)

            // Then
            val requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest::class.java)
            org.mockito.Mockito
                .verify(s3Client)
                .deleteObject(requestCaptor.capture())
            assertEquals(BUCKET, requestCaptor.value.bucket())
            assertEquals("avatars/$AVATAR_ID.png", requestCaptor.value.key())
        }

        @Test
        @DisplayName("key를 추출할 수 없으면 502 예외를 던진다")
        fun `throws bad gateway when key cannot be extracted`() {
            // Given
            val storage = s3AvatarImageStorage(publicBaseUrl = PUBLIC_BASE_URL)

            // When
            val exception =
                assertThrows<AikonException> {
                    storage.delete("https://unknown.example.com/avatar.png")
                }

            // Then
            assertEquals(ErrorCode.AVATAR_IMAGE_DELETE_FAILED, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl 메서드는")
    inner class GeneratePresignedUrl {
        @Test
        @DisplayName("publicBaseUrl 기반 URL에서 key를 추출해 presigned URL을 생성한다")
        fun `returns presigned url extracted from public base url`() {
            // Given
            val storage = s3AvatarImageStorage(publicBaseUrl = PUBLIC_BASE_URL)
            val imageUrl = "$PUBLIC_BASE_URL/avatars/$AVATAR_ID.png"
            val presignedGetObjectRequest = org.mockito.Mockito.mock(PresignedGetObjectRequest::class.java)
            given(presignedGetObjectRequest.url()).willReturn(URI.create(PRESIGNED_URL).toURL())
            given(
                s3Presigner.presignGetObject(org.mockito.ArgumentMatchers.any(GetObjectPresignRequest::class.java)),
            ).willReturn(presignedGetObjectRequest)

            // When
            val result = storage.generatePresignedUrl(imageUrl)

            // Then
            assertEquals(PRESIGNED_URL, result)
            val requestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest::class.java)
            org.mockito.Mockito
                .verify(s3Presigner)
                .presignGetObject(requestCaptor.capture())
            assertEquals(BUCKET, requestCaptor.value.getObjectRequest().bucket())
            assertEquals("avatars/$AVATAR_ID.png", requestCaptor.value.getObjectRequest().key())
        }

        @Test
        @DisplayName("key를 추출할 수 없으면 502 예외를 던진다")
        fun `throws bad gateway when key cannot be extracted`() {
            // Given
            val storage = s3AvatarImageStorage(publicBaseUrl = PUBLIC_BASE_URL)

            // When
            val exception =
                assertThrows<AikonException> {
                    storage.generatePresignedUrl("https://unknown.example.com/avatar.png")
                }

            // Then
            assertEquals(ErrorCode.AVATAR_IMAGE_URL_GENERATION_FAILED, exception.errorCode)
        }
    }

    private fun s3AvatarImageStorage(publicBaseUrl: String): S3AvatarImageStorage =
        S3AvatarImageStorage(
            s3Client = s3Client,
            s3Presigner = s3Presigner,
            region = REGION,
            bucket = BUCKET,
            publicBaseUrl = publicBaseUrl,
            presignedUrlExpirationSeconds = PRESIGNED_URL_EXPIRATION_SECONDS,
        )

    companion object {
        private const val AVATAR_ID = 1L
        private const val REGION = "ap-northeast-2"
        private const val BUCKET = "aikon"
        private const val PUBLIC_BASE_URL = "http://localhost:9000/aikon"
        private const val PRESIGNED_URL_EXPIRATION_SECONDS = 3600L
        private const val PRESIGNED_URL = "https://example.com/avatars/1.png?X-Amz-Signature=signed"
    }
}
