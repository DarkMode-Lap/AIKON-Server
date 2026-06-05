package team.darkmoderap.aikon.domain.avatar.dto

import io.swagger.v3.oas.annotations.media.Schema
import team.darkmoderap.aikon.domain.avatar.entity.enum.GenerationStatus

@Schema(description = "아바타 단건 조회 응답")
data class GetAvatarResDto(
    @Schema(description = "아바타 ID", example = "1")
    val id: Long,
    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,
    @Schema(description = "생성 상태 (WAITING, PROCESSING, COMPLETED, FAILED, RETRYING)")
    val generationStatus: GenerationStatus,
    @Schema(description = "아바타 이미지 URL. 생성 완료 전에는 null", nullable = true)
    val imageUrl: String?,
    @Schema(description = "패스 URL. 생성 완료 전에는 null", nullable = true)
    val passUrl: String?,
    @Schema(description = "QR 코드 URL. 생성 완료 전에는 null", nullable = true)
    val qrUrl: String?,
)
