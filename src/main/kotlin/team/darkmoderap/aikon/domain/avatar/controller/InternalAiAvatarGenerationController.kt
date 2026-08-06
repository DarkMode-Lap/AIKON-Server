package team.darkmoderap.aikon.domain.avatar.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.darkmoderap.aikon.domain.avatar.dto.AvatarGenerationCallbackReqDto
import team.darkmoderap.aikon.domain.avatar.service.HandleAvatarGenerationCallbackService
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.security.MessageDigest

@RestController
@RequestMapping("/internal/ai/avatar-generations")
class InternalAiAvatarGenerationController(
    private val handleAvatarGenerationCallbackService: HandleAvatarGenerationCallbackService,
    @Value("\${internal.callback-secret}") private val callbackSecret: String,
) {
    @PostMapping("/callback")
    @ResponseStatus(HttpStatus.OK)
    fun callback(
        @RequestHeader("X-Internal-Secret") secret: String,
        @RequestBody reqDto: AvatarGenerationCallbackReqDto,
    ) {
        if (!MessageDigest.isEqual(secret.toByteArray(), callbackSecret.toByteArray())) {
            throw AikonException(ErrorCode.INVALID_INTERNAL_SECRET)
        }
        handleAvatarGenerationCallbackService.execute(reqDto)
    }
}
