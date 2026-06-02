package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.web.multipart.MultipartFile
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarResDto

interface CreateAvatarService {
    fun execute(
        reqDto: CreateAvatarReqDto,
        image: MultipartFile,
    ): CreateAvatarResDto
}
