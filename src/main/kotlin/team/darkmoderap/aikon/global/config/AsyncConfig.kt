package team.darkmoderap.aikon.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
    @Bean(name = [ASYNC_EXECUTOR])
    fun asyncExecutor(): Executor = VirtualThreadTaskExecutor("async-executor-")

    override fun getAsyncExecutor(): Executor = asyncExecutor()

    companion object {
        const val ASYNC_EXECUTOR = "asyncExecutor"
    }
}
