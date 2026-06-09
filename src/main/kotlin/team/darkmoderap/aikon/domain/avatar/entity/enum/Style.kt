package team.darkmoderap.aikon.domain.avatar.entity.enum

enum class Style {
    STUDIO,
    ENHANCED,
    ZOOTOPIA,
    TRADITIONAL_HANBOK,
    DISNEY_PIXAR,
    GHIBLI,
    LIGHT_ART,
    ;

    companion object {
        const val SCHEMA_DESCRIPTION =
            "스타일 (STUDIO, ENHANCED, ZOOTOPIA, TRADITIONAL_HANBOK, DISNEY_PIXAR, GHIBLI, LIGHT_ART)"
    }
}
