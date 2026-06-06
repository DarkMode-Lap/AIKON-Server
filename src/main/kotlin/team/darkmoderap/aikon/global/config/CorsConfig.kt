package team.darkmoderap.aikon.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    @Value("\${aikon.frontend.public-base-url}") private val frontendPublicBaseUrl: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        if (frontendPublicBaseUrl.isBlank()) return

        val origins = frontendPublicBaseUrl.split(",").map { it.trim() }.toTypedArray()
        registry
            .addMapping("/**")
            .allowedOrigins(*origins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
    }
}
