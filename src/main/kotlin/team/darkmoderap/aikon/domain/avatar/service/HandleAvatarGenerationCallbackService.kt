package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.dto.AvatarGenerationCallbackReqDto

interface HandleAvatarGenerationCallbackService {
    fun execute(reqDto: AvatarGenerationCallbackReqDto)
}
