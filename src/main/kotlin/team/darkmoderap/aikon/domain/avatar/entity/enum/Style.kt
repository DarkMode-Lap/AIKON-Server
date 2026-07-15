package team.darkmoderap.aikon.domain.avatar.entity.enum

enum class Style {
    ENHANCED,
    ZOOTOPIA,
    TRADITIONAL_HANBOK,
    DISNEY_PIXAR,
    GHIBLI,
    LIGHT_ART,
    ;

    companion object {
        const val SCHEMA_DESCRIPTION =
            "스타일 (ENHANCED, ZOOTOPIA, TRADITIONAL_HANBOK, DISNEY_PIXAR, GHIBLI, LIGHT_ART)"
    }
}
