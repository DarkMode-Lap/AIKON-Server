package team.darkmoderap.aikon.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean
    fun fastApiRestClient(
        @Value("\${fastapi.base-url}") baseUrl: String,
    ): RestClient {
        val requestFactory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(5000)
                setReadTimeout(10000)
            }
        return RestClient
            .builder()
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build()
    }
}
