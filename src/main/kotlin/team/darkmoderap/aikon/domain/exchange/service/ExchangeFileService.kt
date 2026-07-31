package team.darkmoderap.aikon.domain.exchange.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class ExchangeFileService(
    @Qualifier("exchangeS3Client") private val s3Client: S3Client,
    @Value("\${exchange.minio.bucket}") private val bucket: String,
) {
    fun uploadIncoming(
        file: MultipartFile,
        fileName: String = "Aikon500.png",
    ) {
        val request =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key("incoming/$fileName")
                .contentType(file.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .build()

        file.inputStream.use { s3Client.putObject(request, RequestBody.fromInputStream(it, file.size)) }
    }

    fun downloadOutgoing(fileName: String = "report500.png"): ByteArray {
        val request =
            GetObjectRequest
                .builder()
                .bucket(bucket)
                .key("outgoing/$fileName")
                .build()

        return s3Client.getObjectAsBytes(request).asByteArray()
    }
}
