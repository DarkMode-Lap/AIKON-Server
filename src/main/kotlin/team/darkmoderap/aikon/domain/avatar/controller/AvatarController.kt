package team.darkmoderap.aikon.domain.avatar.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.service.DeleteAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateDefaultStyleService

@RestController
@RequestMapping("/avatars")
class AvatarController(
    private val updateAvatarService: UpdateAvatarService,
    private val updateDefaultStyleService: UpdateDefaultStyleService,
    private val deleteAvatarService: DeleteAvatarService,
) {
    @PatchMapping("/{avatarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateAvatar(
        @PathVariable avatarId: Long,
        @Valid @RequestBody reqDto: UpdateAvatarReqDto,
    ) {
        updateAvatarService.execute(avatarId, reqDto)
    }

    // TODO: 어드민 권한 체크 추가 (인증 인프라 도입 후)
    @PatchMapping("/style")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateDefaultStyle(
        @Valid @RequestBody reqDto: UpdateDefaultStyleReqDto,
    ) {
        updateDefaultStyleService.execute(reqDto)
    }

    @DeleteMapping("/{avatarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAvatar(
        @PathVariable avatarId: Long,
    ) {
        deleteAvatarService.execute(avatarId)
    }
}
