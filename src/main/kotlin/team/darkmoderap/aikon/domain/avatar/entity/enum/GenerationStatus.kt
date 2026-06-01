package team.darkmoderap.aikon.domain.avatar.entity.enum

enum class GenerationStatus {
    WAITING,
    PROCESSING,
    COMPLETED,
    FAILED,
    RETRYING,
    ;

    fun isGenerating(): Boolean = this == PROCESSING || this == RETRYING
}
