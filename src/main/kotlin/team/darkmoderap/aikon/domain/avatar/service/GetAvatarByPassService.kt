package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto

interface GetAvatarByPassService {
    fun execute(passUrl: String): GetAvatarResDto
}
