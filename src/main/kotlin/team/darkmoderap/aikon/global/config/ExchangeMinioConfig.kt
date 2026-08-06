package team.darkmoderap.aikon.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class ExchangeMinioConfig {
    @Bean("exchangeS3Client")
    fun exchangeS3Client(
        @Value("\${exchange.minio.endpoint}") endpoint: String,
        @Value("\${exchange.minio.access-key}") accessKey: String,
        @Value("\${exchange.minio.secret-key}") secretKey: String,
    ): S3Client =
        S3Client
            .builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.US_EAST_1)
            .forcePathStyle(true)
            .build()
}
