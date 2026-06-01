package team.darkmoderap.aikon.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3Config {
    @Bean
    fun s3Client(
        @Value("\${aws.region}") region: String,
    ): S3Client =
        S3Client
            .builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
}
