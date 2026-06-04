package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import team.darkmoderap.aikon.global.common.error.AikonException
import team.darkmoderap.aikon.global.common.error.ErrorCode
import java.util.Base64

@Service
class GeminiAvatarImageGenerator(
    private val promptProvider: AvatarImagePromptProvider,
    @Value("\${gemini.api-key}") private val apiKey: String,
    @Value("\${gemini.image-model}") private val imageModel: String,
) : AvatarImageGenerator {
    private val restClient =
        RestClient
            .builder()
            .baseUrl(BASE_URL)
            .build()

    override fun generate(command: AvatarImageGenerationCommand): GeneratedAvatarImage {
        if (apiKey.isBlank()) {
            throw AikonException(ErrorCode.AVATAR_IMAGE_GENERATION_FAILED)
        }

        val response =
            restClient
                .post()
                .uri("/models/$imageModel:generateContent")
                .header(API_KEY_HEADER, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createRequest(command))
                .retrieve()
                .body(GeminiGenerateContentResponse::class.java)
                ?: throw AikonException(ErrorCode.AVATAR_IMAGE_GENERATION_FAILED)

        val inlineData =
            response.candidates
                .flatMap { candidate -> candidate.content.parts }
                .mapNotNull { part -> part.inlineData }
                .firstOrNull()
                ?: throw AikonException(ErrorCode.AVATAR_IMAGE_GENERATION_FAILED)

        return GeneratedAvatarImage(
            bytes = Base64.getDecoder().decode(inlineData.data),
            mimeType = inlineData.mimeType,
        )
    }

    private fun createRequest(command: AvatarImageGenerationCommand): GeminiGenerateContentRequest =
        GeminiGenerateContentRequest(
            contents =
                listOf(
                    GeminiContent(
                        parts =
                            listOf(
                                GeminiPart(text = promptProvider.getPrompt(command.style, command.gender, command.ageRange)),
                                GeminiPart(
                                    inlineData =
                                        GeminiInlineData(
                                            mimeType = command.sourceMimeType,
                                            data = Base64.getEncoder().encodeToString(command.sourceImage),
                                        ),
                                ),
                            ),
                    ),
                ),
        )

    private data class GeminiGenerateContentRequest(
        val contents: List<GeminiContent>,
    )

    private data class GeminiGenerateContentResponse(
        val candidates: List<GeminiCandidate> = emptyList(),
    )

    private data class GeminiCandidate(
        val content: GeminiContent = GeminiContent(),
    )

    private data class GeminiContent(
        val parts: List<GeminiPart> = emptyList(),
    )

    private data class GeminiPart(
        val text: String? = null,
        val inlineData: GeminiInlineData? = null,
    )

    private data class GeminiInlineData(
        val mimeType: String,
        val data: String,
    )

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val API_KEY_HEADER = "x-goog-api-key"
    }
}
