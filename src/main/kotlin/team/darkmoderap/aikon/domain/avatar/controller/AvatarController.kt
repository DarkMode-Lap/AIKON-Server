package team.darkmoderap.aikon.domain.avatar.controller

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarResDto
import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.service.CreateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.DeleteAvatarService
import team.darkmoderap.aikon.domain.avatar.service.GetAvatarService
import team.darkmoderap.aikon.domain.avatar.service.SubscribeAvatarChangesService
import team.darkmoderap.aikon.domain.avatar.service.UpdateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateDefaultStyleService
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode

@RestController
@RequestMapping("/avatars")
class AvatarController(
    private val createAvatarService: CreateAvatarService,
    private val getAvatarService: GetAvatarService,
    private val subscribeAvatarChangesService: SubscribeAvatarChangesService,
    private val updateAvatarService: UpdateAvatarService,
    private val updateDefaultStyleService: UpdateDefaultStyleService,
    private val deleteAvatarService: DeleteAvatarService,
    private val validator: Validator,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createAvatar(
        @RequestPart("reqDto") reqDtoPart: MultipartFile,
        @RequestPart image: MultipartFile,
    ): CreateAvatarResDto = createAvatarService.execute(parseCreateAvatarReqDto(reqDtoPart), image)

    @GetMapping("/{avatarId}")
    fun getAvatar(
        @PathVariable avatarId: Long,
    ): GetAvatarResDto = getAvatarService.execute(avatarId)

    @GetMapping("/changes", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribeAvatarChanges(): SseEmitter = subscribeAvatarChangesService.execute()

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

    private fun parseCreateAvatarReqDto(reqDtoPart: MultipartFile): CreateAvatarReqDto {
        val rawReqDto =
            reqDtoPart.inputStream
                .bufferedReader(Charsets.UTF_8)
                .use { reader -> reader.readText() }
        val reqDto =
            try {
                objectMapper.readValue(rawReqDto, CreateAvatarReqDto::class.java)
            } catch (exception: JsonProcessingException) {
                throw AikonException(ErrorCode.INVALID_INPUT_VALUE, cause = exception)
            }

        validateCreateAvatarReqDto(reqDto)

        return reqDto
    }

    private fun validateCreateAvatarReqDto(reqDto: CreateAvatarReqDto) {
        val violations = validator.validate(reqDto)

        if (violations.isEmpty()) {
            return
        }

        val bindingResult = BeanPropertyBindingResult(reqDto, "reqDto")
        violations.forEach { violation ->
            bindingResult.addError(
                FieldError(
                    "reqDto",
                    violation.propertyPath.toString(),
                    violation.invalidValue,
                    false,
                    null,
                    null,
                    violation.message,
                ),
            )
        }

        throw BindException(bindingResult)
    }
}
