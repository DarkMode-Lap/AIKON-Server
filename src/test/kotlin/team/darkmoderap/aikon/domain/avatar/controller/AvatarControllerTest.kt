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
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException
import team.darkmoderap.aikon.domain.avatar.dto.UpdateAvatarReqDto
import team.darkmoderap.aikon.domain.avatar.dto.UpdateDefaultStyleReqDto
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.domain.avatar.service.DeleteAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateAvatarService
import team.darkmoderap.aikon.domain.avatar.service.UpdateDefaultStyleService

class AvatarControllerTest {
    private val updateAvatarService = mock(UpdateAvatarService::class.java)
    private val updateDefaultStyleService = mock(UpdateDefaultStyleService::class.java)
    private val deleteAvatarService = mock(DeleteAvatarService::class.java)

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(
                AvatarController(updateAvatarService, updateDefaultStyleService, deleteAvatarService),
            ).build()

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
        @DisplayName("서비스가 404를 던지면 404를 반환한다")
        fun `returns 404 when service throws not found`() {
            // Given
            willThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
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
        @DisplayName("서비스가 409를 던지면 409를 반환한다")
        fun `returns 409 when service throws conflict`() {
            // Given
            willThrow(ResponseStatusException(HttpStatus.CONFLICT))
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
        @DisplayName("서비스가 404를 던지면 404를 반환한다")
        fun `returns 404 when service throws not found`() {
            // Given
            willThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
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
