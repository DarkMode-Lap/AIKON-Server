package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AvatarQrUrlProvider(
    @Value("\${aikon.frontend.public-base-url:}")
    publicBaseUrl: String,
) {
    private val baseUrl = publicBaseUrl.trim().trimEnd('/')

    fun create(passUrl: String): String? {
        if (baseUrl.isBlank()) {
            return null
        }

        return "$baseUrl/pass/$passUrl"
    }
}
