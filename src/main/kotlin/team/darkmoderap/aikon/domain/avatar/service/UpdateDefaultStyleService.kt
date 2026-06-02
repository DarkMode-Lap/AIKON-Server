package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto

interface UpdateDefaultStyleService {
    fun execute(reqDto: UpdateDefaultStyleReqDto)
}
