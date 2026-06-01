package team.darkmoderap.aikon.global.common.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Health", description = "서버 상태 확인")
@RestController
class HealthController {
    @Operation(summary = "헬스체크", description = "서버 상태와 현재 시각을 반환합니다.")
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        val response =
            mapOf(
                "status" to "UP",
                "timestamp" to Instant.now().toString(),
            )
        return ResponseEntity.ok(response)
    }
}
