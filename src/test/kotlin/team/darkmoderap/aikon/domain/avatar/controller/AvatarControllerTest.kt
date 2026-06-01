package team.darkmoderap.aikon.domain.avatar.controller

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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.service.DeleteAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateDefaultStyleService
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import team.darkmoderap.aikon.global.common.error.handler.GlobalExceptionHandler

class AvatarControllerTest {
    private val updateAvatarService = mock(UpdateAvatarService::class.java)
    private val updateDefaultStyleService = mock(UpdateDefaultStyleService::class.java)
    private val deleteAvatarService = mock(DeleteAvatarService::class.java)

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(
                AvatarController(updateAvatarService, updateDefaultStyleService, deleteAvatarService),
            ).setControllerAdvice(GlobalExceptionHandler())
            .build()

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
