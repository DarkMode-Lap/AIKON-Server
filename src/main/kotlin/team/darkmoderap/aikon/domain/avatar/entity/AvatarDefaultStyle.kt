package team.darkmoderap.aikon.domain.avatar.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style
import team.darkmoderap.aikon.global.common.entity.BaseEntity

/**
 * 서버 전역 기본 스타일 설정(싱글톤). 사용자가 아바타 생성 시 스타일을 고르지 않으면 이 값이 적용된다.
 * 고정 PK(SINGLETON_ID)로 단 한 행만 존재하도록 강제한다.
 */
@Entity
@Table(name = "avatar_default_style")
class AvatarDefaultStyle(
    @Enumerated(EnumType.STRING)
    @Column(name = "default_style", nullable = false)
    var defaultStyle: Style,
    @Id
    val id: Long = SINGLETON_ID,
) : BaseEntity() {
    fun updateStyle(style: Style) {
        this.defaultStyle = style
    }

    companion object {
        const val SINGLETON_ID = 1L
    }
}
