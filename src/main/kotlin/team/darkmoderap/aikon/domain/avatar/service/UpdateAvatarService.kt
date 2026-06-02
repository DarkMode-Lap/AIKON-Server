package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto

interface UpdateAvatarService {
    fun execute(
        avatarId: Long,
        reqDto: UpdateAvatarReqDto,
    )
}
