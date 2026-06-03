package team.darkmoderap.aikon.domain.avatar.event

import team.darkmoderap.aikon.domain.avatar.service.AvatarSourceImage

class AvatarCreatedEvent(
    val avatarId: Long,
    val sourceImage: AvatarSourceImage,
)
