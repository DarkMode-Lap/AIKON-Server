package team.darkmoderap.aikon.domain.avatar.controller

import jakarta.validation.Validation
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.willThrow
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.CreateAvatarResDto
import team.darkmoderap.aikon.domain.avatar.dto.GetAvatarResDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.service.CreateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.DeleteAvatarService
import team.darkmoderap.aikon.domain.avatar.service.GetAvatarService
import team.darkmoderap.aikon.domain.avatar.service.SubscribeAvatarChangesService
import team.darkmoderap.aikon.domain.avatar.service.UpdateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateDefaultStyleService
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import team.darkmoderap.aikon.global.common.error.handler.GlobalExceptionHandler

class AvatarControllerTest {
    private val createAvatarService = mock(CreateAvatarService::class.java)
    private val getAvatarService = mock(GetAvatarService::class.java)
    private val subscribeAvatarChangesService = mock(SubscribeAvatarChangesService::class.java)
    private val updateAvatarService = mock(UpdateAvatarService::class.java)
    private val updateDefaultStyleService = mock(UpdateDefaultStyleService::class.java)
    private val deleteAvatarService = mock(DeleteAvatarService::class.java)

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(
                AvatarController(
                    createAvatarService,
                    getAvatarService,
                    subscribeAvatarChangesService,
                    updateAvatarService,
                    updateDefaultStyleService,
                    deleteAvatarService,
                    Validation.buildDefaultValidatorFactory().validator,
                ),
            ).setControllerAdvice(GlobalExceptionHandler())
            .build()

    @Nested
    @DisplayName("POST /avatars 는")
    inner class CreateAvatar {
        @Test
        @DisplayName("유효한 요청이면 201을 반환하고 서비스를 호출한다")
        fun `returns 201 when request is valid`() {
            // Given
            Mockito
                .`when`(createAvatarService.execute(anyCreateReqDto(), anyImage()))
                .thenReturn(CreateAvatarResDto(id = AVATAR_ID, generationStatus = GenerationStatus.PROCESSING))

            // When & Then
            mockMvc
                .perform(
                    multipart("/avatars")
                        .part(reqDtoPart())
                        .file(imagePart()),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(AVATAR_ID))
                .andExpect(jsonPath("$.generationStatus").value("PROCESSING"))

            verify(createAvatarService).execute(anyCreateReqDto(), anyImage())
        }

        @Test
        @DisplayName("reqDto 파트가 octet-stream이어도 201을 반환하고 서비스를 호출한다")
        fun `returns 201 when reqDto part content type is octet stream`() {
            // Given
            Mockito
                .`when`(createAvatarService.execute(anyCreateReqDto(), anyImage()))
                .thenReturn(CreateAvatarResDto(id = AVATAR_ID, generationStatus = GenerationStatus.PROCESSING))

            // When & Then
            mockMvc
                .perform(
                    multipart("/avatars")
                        .part(reqDtoPart(contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .file(imagePart()),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(AVATAR_ID))
                .andExpect(jsonPath("$.generationStatus").value("PROCESSING"))

            verify(createAvatarService).execute(anyCreateReqDto(), anyImage())
        }

        @Test
        @DisplayName("이미지 파트가 누락되면 400을 반환하고 서비스를 호출하지 않는다")
        fun `returns 400 when image part is missing`() {
            // Given
            val reqDto = """{"nickname":"새아바타","gender":"FEMALE","style":"GHIBLI","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    multipart("/avatars")
                        .part(reqDtoPart(reqDto)),
                ).andExpect(status().isBadRequest)

            verify(createAvatarService, never()).execute(anyCreateReqDto(), anyImage())
        }

        @Test
        @DisplayName("잘못된 enum 값이면 400을 반환하고 서비스를 호출하지 않는다")
        fun `returns 400 when enum value is invalid`() {
            // Given
            val reqDto = """{"nickname":"새아바타","gender":"UNKNOWN","style":"GHIBLI","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    multipart("/avatars")
                        .part(reqDtoPart(reqDto))
                        .file(imagePart()),
                ).andExpect(status().isBadRequest)

            verify(createAvatarService, never()).execute(anyCreateReqDto(), anyImage())
        }
    }

    @Nested
    @DisplayName("GET /avatars/{avatarId} 는")
    inner class GetAvatar {
        @Test
        @DisplayName("아바타가 존재하면 200과 응답 본문을 반환한다")
        fun `returns 200 when avatar exists`() {
            // Given
            Mockito
                .`when`(getAvatarService.execute(AVATAR_ID))
                .thenReturn(
                    GetAvatarResDto(
                        id = AVATAR_ID,
                        nickname = "새아바타",
                        generationStatus = GenerationStatus.COMPLETED,
                        imageUrl = "https://example.com/avatar.png",
                        passUrl = "Aikon500",
                        qrUrl = "https://aikon.example.com/pass/Aikon500",
                    ),
                )

            // When & Then
            mockMvc
                .perform(get("/avatars/{avatarId}", AVATAR_ID))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(AVATAR_ID))
                .andExpect(jsonPath("$.nickname").value("새아바타"))
                .andExpect(jsonPath("$.generationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.passUrl").value("Aikon500"))
                .andExpect(jsonPath("$.qrUrl").value("https://aikon.example.com/pass/Aikon500"))
        }

        @Test
        @DisplayName("아바타가 없으면 404를 반환한다")
        fun `returns 404 when avatar not found`() {
            // Given
            willThrow(AikonException(ErrorCode.AVATAR_NOT_FOUND))
                .given(getAvatarService)
                .execute(AVATAR_ID)

            // When & Then
            mockMvc
                .perform(get("/avatars/{avatarId}", AVATAR_ID))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("GET /avatars/changes 는")
    inner class SubscribeAvatarChanges {
        @Test
        @DisplayName("SSE 구독 요청이면 200을 반환하고 서비스를 호출한다")
        fun `returns 200 when subscription starts`() {
            // Given
            Mockito
                .`when`(subscribeAvatarChangesService.execute())
                .thenReturn(SseEmitter())

            // When & Then
            mockMvc
                .perform(get("/avatars/changes").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk)

            verify(subscribeAvatarChangesService).execute()
        }
    }

    @Nested
    @DisplayName("PATCH /avatars/{avatarId} 는")
    inner class UpdateAvatar {
        @Test
        @DisplayName("유효한 요청이면 204를 반환하고 서비스를 호출한다")
        fun `returns 204 when request is valid`() {
            // Given
            val body = """{"nickname":"새이름","gender":"FEMALE","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", AVATAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isNoContent)

            verify(updateAvatarService).execute(anyLong(), anyReqDto())
        }

        @Test
        @DisplayName("아바타가 없으면 404를 반환한다")
        fun `returns 404 when avatar not found`() {
            // Given
            willThrow(AikonException(ErrorCode.AVATAR_NOT_FOUND))
                .given(updateAvatarService)
                .execute(anyLong(), anyReqDto())
            val body = """{"nickname":"새이름","gender":"FEMALE","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", AVATAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("생성 진행 중이면 409를 반환한다")
        fun `returns 409 when avatar is generating`() {
            // Given
            willThrow(AikonException(ErrorCode.AVATAR_GENERATION_IN_PROGRESS))
                .given(updateAvatarService)
                .execute(anyLong(), anyReqDto())
            val body = """{"nickname":"새이름","gender":"FEMALE","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", AVATAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isConflict)
        }

        @Test
        @DisplayName("nickname이 공백이면 400을 반환하고 서비스를 호출하지 않는다")
        fun `returns 400 when nickname is blank`() {
            // Given
            val body = """{"nickname":"   ","gender":"FEMALE","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", AVATAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isBadRequest)

            verify(updateAvatarService, never()).execute(anyLong(), anyReqDto())
        }

        @Test
        @DisplayName("잘못된 enum 값이면 400을 반환하고 서비스를 호출하지 않는다")
        fun `returns 400 when enum value is invalid`() {
            // Given
            val body = """{"nickname":"새이름","gender":"UNKNOWN","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", AVATAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isBadRequest)

            verify(updateAvatarService, never()).execute(anyLong(), anyReqDto())
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun `returns 400 when required field is missing`() {
            // Given
            val body = """{"nickname":"새이름","gender":"FEMALE"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", AVATAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isBadRequest)

            verify(updateAvatarService, never()).execute(anyLong(), anyReqDto())
        }

        @Test
        @DisplayName("경로 변수가 숫자가 아니면 400을 반환한다")
        fun `returns 400 when path variable is not a number`() {
            // Given
            val body = """{"nickname":"새이름","gender":"FEMALE","ageRange":"AGE_20_PLUS"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/{avatarId}", "not-a-number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isBadRequest)

            verify(updateAvatarService, never()).execute(anyLong(), anyReqDto())
        }
    }

    @Nested
    @DisplayName("PATCH /avatars/style 는")
    inner class UpdateDefaultStyle {
        @Test
        @DisplayName("유효한 요청이면 204를 반환하고 서비스를 호출한다")
        fun `returns 204 when request is valid`() {
            // Given
            val body = """{"style":"GHIBLI"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/style")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isNoContent)

            verify(updateDefaultStyleService).execute(anyDefaultStyleReqDto())
        }

        @Test
        @DisplayName("잘못된 스타일 값이면 400을 반환하고 서비스를 호출하지 않는다")
        fun `returns 400 when style is invalid`() {
            // Given
            val body = """{"style":"UNKNOWN"}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/style")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isBadRequest)

            verify(updateDefaultStyleService, never()).execute(anyDefaultStyleReqDto())
        }

        @Test
        @DisplayName("style 필드가 누락되면 400을 반환한다")
        fun `returns 400 when style is missing`() {
            // Given
            val body = """{}"""

            // When & Then
            mockMvc
                .perform(
                    patch("/avatars/style")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body),
                ).andExpect(status().isBadRequest)

            verify(updateDefaultStyleService, never()).execute(anyDefaultStyleReqDto())
        }
    }

    @Nested
    @DisplayName("DELETE /avatars/{avatarId} 는")
    inner class DeleteAvatar {
        @Test
        @DisplayName("유효한 요청이면 204를 반환하고 서비스를 호출한다")
        fun `returns 204 when request is valid`() {
            // When & Then
            mockMvc
                .perform(delete("/avatars/{avatarId}", AVATAR_ID))
                .andExpect(status().isNoContent)

            verify(deleteAvatarService).execute(anyLong())
        }

        @Test
        @DisplayName("아바타가 없으면 404를 반환한다")
        fun `returns 404 when avatar not found`() {
            // Given
            willThrow(AikonException(ErrorCode.AVATAR_NOT_FOUND))
                .given(deleteAvatarService)
                .execute(anyLong())

            // When & Then
            mockMvc
                .perform(delete("/avatars/{avatarId}", AVATAR_ID))
                .andExpect(status().isNotFound)
        }
    }

    companion object {
        private const val AVATAR_ID = 1L

        private fun anyCreateReqDto(): CreateAvatarReqDto {
            Mockito.any(CreateAvatarReqDto::class.java)
            return CreateAvatarReqDto(
                nickname = "dummy",
                gender = Gender.MALE,
                style = Style.STUDIO,
                ageRange = AgeRange.AGE_0_7,
            )
        }

        private fun anyImage(): MultipartFile {
            Mockito.any(MultipartFile::class.java)
            return imagePart()
        }

        private fun reqDtoPart(
            content: String = """{"nickname":"새아바타","gender":"FEMALE","style":"GHIBLI","ageRange":"AGE_20_PLUS"}""",
            contentType: String = MediaType.APPLICATION_JSON_VALUE,
        ): MockPart =
            MockPart(
                "reqDto",
                content.toByteArray(),
            ).apply {
                headers.contentType = MediaType.parseMediaType(contentType)
            }

        private fun imagePart(): MockMultipartFile =
            MockMultipartFile(
                "image",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                byteArrayOf(1, 2, 3),
            )

        private fun anyReqDto(): UpdateAvatarReqDto {
            Mockito.any(UpdateAvatarReqDto::class.java)
            return UpdateAvatarReqDto(nickname = "dummy", gender = Gender.MALE, ageRange = AgeRange.AGE_0_7)
        }

        private fun anyDefaultStyleReqDto(): UpdateDefaultStyleReqDto {
            Mockito.any(UpdateDefaultStyleReqDto::class.java)
            return UpdateDefaultStyleReqDto(style = Style.STUDIO)
        }
    }
}
