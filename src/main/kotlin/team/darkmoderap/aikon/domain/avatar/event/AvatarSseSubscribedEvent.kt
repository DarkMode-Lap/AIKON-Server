package team.darkmoderap.aikon.domain.avatar.event

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class AvatarSseSubscribedEvent(
    val emitter: SseEmitter,
)
