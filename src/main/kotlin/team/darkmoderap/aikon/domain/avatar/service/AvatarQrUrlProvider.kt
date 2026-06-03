package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AvatarQrUrlProvider(
    @Value("\${aikon.frontend.public-base-url:}")
    private val publicBaseUrl: String,
) {
    fun create(passUrl: String): String? {
        val baseUrl = publicBaseUrl.trim().trimEnd('/')

        if (baseUrl.isBlank()) {
            return null
        }

        return "$baseUrl/pass/$passUrl"
    }
}
