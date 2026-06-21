package team.darkmoderap.aikon.domain.avatar.service

import team.darkmoderap.aikon.domain.avatar.dto.CreateFeedbackReqDto

interface CreateFeedbackService {
    fun execute(
        avatarId: Long,
        reqDto: CreateFeedbackReqDto,
    )
}
