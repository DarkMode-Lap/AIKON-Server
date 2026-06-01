package team.darkmoderap.aikon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class AikonApplication

fun main(args: Array<String>) {
    runApplication<AikonApplication>(*args)
}
