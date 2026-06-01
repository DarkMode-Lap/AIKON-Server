package team.darkmoderap.aikon.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class S3Config {
    @Bean
    fun s3Client(
        @Value("\${aws.region}") region: String,
        @Value("\${aws.s3.endpoint}") endpoint: String,
        @Value("\${aws.s3.path-style-access-enabled}") pathStyleAccessEnabled: Boolean,
    ): S3Client {
        val builder =
            S3Client
                .builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .serviceConfiguration(
                    S3Configuration
                        .builder()
                        .pathStyleAccessEnabled(pathStyleAccessEnabled)
                        .build(),
                )

        if (endpoint.isNotBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }

        return builder
            .build()
    }
}
