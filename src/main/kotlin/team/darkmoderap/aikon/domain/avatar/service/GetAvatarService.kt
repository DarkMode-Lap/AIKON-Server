package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto

interface GetAvatarService {
    fun execute(avatarId: Long): GetAvatarResDto
}
