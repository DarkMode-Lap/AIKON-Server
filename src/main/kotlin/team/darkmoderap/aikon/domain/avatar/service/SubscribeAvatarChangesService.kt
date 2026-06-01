package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SubscribeAvatarChangesService {
    fun execute(): SseEmitter
}
