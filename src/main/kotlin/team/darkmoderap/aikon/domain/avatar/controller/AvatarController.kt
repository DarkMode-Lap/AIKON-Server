package team.darkmoderap.aikon.domain.avatar.controller

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
import team.darkmoderap.aikon.domain.avatar.dto.AvatarChangeResDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarResDto
import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.service.CreateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.DeleteAllAvatarsService
import team.darkmoderap.aikon.domain.avatar.service.DeleteAvatarService
import team.darkmoderap.aikon.domain.avatar.service.GetAvatarByPassService
import team.darkmoderap.aikon.domain.avatar.service.GetAvatarService
import team.darkmoderap.aikon.domain.avatar.service.SubscribeAvatarChangesService
import team.darkmoderap.aikon.domain.avatar.service.UpdateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateDefaultStyleService
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import team.darkmoderap.aikon.global.common.error.dto.ErrorResponse

@Tag(name = "Avatar", description = "아바타 API")
@RestController
@RequestMapping("/avatars")
class AvatarController(
    private val createAvatarService: CreateAvatarService,
    private val getAvatarService: GetAvatarService,
    private val getAvatarByPassService: GetAvatarByPassService,
    private val subscribeAvatarChangesService: SubscribeAvatarChangesService,
    private val updateAvatarService: UpdateAvatarService,
    private val updateDefaultStyleService: UpdateDefaultStyleService,
    private val deleteAvatarService: DeleteAvatarService,
    private val deleteAllAvatarsService: DeleteAllAvatarsService,
    private val validator: Validator,
    private val objectMapper: ObjectMapper,
) {
    @Operation(summary = "아바타 생성")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "패스 코드 소진 또는 배정 실패",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createAvatar(
        @RequestPart("reqDto") rawReqDto: String,
        @RequestPart image: MultipartFile,
    ): CreateAvatarResDto = createAvatarService.execute(parseCreateAvatarReqDto(rawReqDto), image)

    @Operation(summary = "아바타 단건 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(
            responseCode = "404",
            description = "아바타를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/{avatarId}")
    fun getAvatar(
        @PathVariable avatarId: Long,
    ): GetAvatarResDto = getAvatarService.execute(avatarId)

    @Operation(summary = "패스 URL로 아바타 단건 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(
            responseCode = "404",
            description = "아바타를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @GetMapping("/pass/{passUrl}")
    fun getAvatarByPassUrl(
        @PathVariable passUrl: String,
    ): GetAvatarResDto = getAvatarByPassService.execute(passUrl)

    @Operation(
        summary = "아바타 목록 변경 SSE 구독",
        description = "구독 즉시 전체 아바타 목록을 전송하고, 이후 목록 변경 시마다 전체 목록을 재전송합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "SSE 스트림 연결 성공",
        content = [
            Content(
                mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                array = ArraySchema(schema = Schema(implementation = AvatarChangeResDto::class)),
            ),
        ],
    )
    @GetMapping("/changes")
    fun subscribeAvatarChanges(): ResponseEntity<SseEmitter> =
        ResponseEntity
            .ok()
            .header("X-Accel-Buffering", "no")
            .body(subscribeAvatarChangesService.execute())

    @Operation(summary = "아바타 수정")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "수정 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "404",
            description = "아바타를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "아바타 생성 중 수정 불가",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PatchMapping("/{avatarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateAvatar(
        @PathVariable avatarId: Long,
        @Valid @RequestBody reqDto: UpdateAvatarReqDto,
    ) {
        updateAvatarService.execute(avatarId, reqDto)
    }

    // TODO: 어드민 권한 체크 추가 (인증 인프라 도입 후)
    @Operation(summary = "기본 스타일 변경")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "변경 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PatchMapping("/style")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateDefaultStyle(
        @Valid @RequestBody reqDto: UpdateDefaultStyleReqDto,
    ) {
        updateDefaultStyleService.execute(reqDto)
    }

    @Operation(summary = "아바타 삭제")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(
            responseCode = "404",
            description = "아바타를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "409",
            description = "아바타 생성 중 삭제 불가",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @DeleteMapping("/{avatarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAvatar(
        @PathVariable avatarId: Long,
    ) {
        deleteAvatarService.execute(avatarId)
    }

    // TODO: 어드민 권한 체크 추가 (인증 인프라 도입 후)
    @Operation(summary = "아바타 전체 삭제")
    @ApiResponse(responseCode = "204", description = "전체 삭제 성공")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAllAvatars() {
        deleteAllAvatarsService.execute()
    }

    private fun parseCreateAvatarReqDto(rawReqDto: String): CreateAvatarReqDto {
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
