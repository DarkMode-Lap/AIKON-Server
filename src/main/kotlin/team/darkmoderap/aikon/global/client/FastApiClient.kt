package team.darkmoderap.aikon.global.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import team.darkmoderap.aikon.domain.avatar.dto.FastApiFeedbackReqDto
import team.darkmoderap.aikon.domain.avatar.dto.FastApiGenerationReqDto
import team.darkmoderap.aikon.domain.avatar.dto.FastApiGenerationResDto
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.concurrent.CompletableFuture

@Component
class FastApiClient(
    private val fastApiRestClient: RestClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun requestAvatarGeneration(reqDto: FastApiGenerationReqDto): FastApiGenerationResDto {
        try {
            return fastApiRestClient
                .post()
                .uri("/ai/avatar-generations")
                .body(reqDto)
                .retrieve()
                .body(FastApiGenerationResDto::class.java)
                ?: throw AikonException(ErrorCode.FASTAPI_REQUEST_FAILED)
        } catch (e: AikonException) {
            throw e
        } catch (e: Exception) {
            throw AikonException(ErrorCode.FASTAPI_REQUEST_FAILED, cause = e)
        }
    }

    fun sendFeedback(reqDto: FastApiFeedbackReqDto) {
        CompletableFuture.runAsync {
            try {
                fastApiRestClient
                    .post()
                    .uri("/ai/feedbacks")
                    .body(reqDto)
                    .retrieve()
                    .toBodilessEntity()
            } catch (e: Exception) {
                logger.warn("Failed to send feedback to FastAPI for avatar {}", reqDto.avatarId, e)
            }
        }
    }
}
