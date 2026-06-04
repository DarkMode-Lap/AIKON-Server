package team.darkmoderap.aikon.domain.avatar.service

import org.springframework.stereotype.Component
import team.darkmoderap.aikon.domain.avatar.entity.enum.AgeRange
import team.darkmoderap.aikon.domain.avatar.entity.enum.Gender
import team.darkmoderap.aikon.domain.avatar.entity.enum.Style

@Component
class AvatarImagePromptProvider {
    fun getPrompt(
        style: Style,
        gender: Gender,
        ageRange: AgeRange,
    ): String {
        val subject = buildSubjectPhrase(gender, ageRange)
        val ageHint = buildAgeHint(ageRange)

        return when (style) {
            Style.STUDIO -> buildStudioPrompt(subject, ageHint)
            Style.ZOOTOPIA -> buildZootopiaPrompt(subject, ageHint)
            Style.TRADITIONAL_HANBOK -> buildHanbokPrompt(subject, gender, ageRange)
            Style.DISNEY_PIXAR -> buildPixarPrompt(subject, ageHint)
            Style.GHIBLI -> buildGhibliPrompt(subject, ageHint)
            Style.LIGHT_ART -> buildLightArtPrompt(subject, ageHint)
        }
    }

    private fun buildSubjectPhrase(
        gender: Gender,
        ageRange: AgeRange,
    ): String {
        val genderWord =
            when {
                gender == Gender.MALE && ageRange == AgeRange.AGE_20_PLUS -> "남성"
                gender == Gender.FEMALE && ageRange == AgeRange.AGE_20_PLUS -> "여성"
                gender == Gender.MALE -> "남자"
                else -> "여자"
            }
        val label =
            when (ageRange) {
                AgeRange.AGE_0_7 -> "0~7세 $genderWord 아이"
                AgeRange.AGE_8_13 -> "8~13세 $genderWord 어린이"
                AgeRange.AGE_14_19 -> "14~19세 $genderWord 청소년"
                AgeRange.AGE_20_PLUS -> "$genderWord"
            }
        val particle = if ((label.last().code - 0xAC00) % 28 != 0) "을" else "를"
        return label + particle
    }

    private fun buildAgeHint(ageRange: AgeRange): String =
        when (ageRange) {
            AgeRange.AGE_0_7 -> {
                "영유아 특유의 통통하고 둥근 볼, 크고 초롱초롱한 눈, 작고 귀여운 코와 입 비율을 강조해줘."
            }

            AgeRange.AGE_8_13 -> {
                "어린이 특유의 밝고 생기 넘치는 표정, 동그란 얼굴형, 순수한 눈빛을 잘 살려줘."
            }

            AgeRange.AGE_14_19 -> {
                "청소년의 생기 있고 활기찬 분위기, 젊고 세련된 감각을 자연스럽게 반영해줘."
            }

            AgeRange.AGE_20_PLUS -> {
                "성인의 자연스럽고 완성된 얼굴 비율, 세련되고 성숙한 분위기를 유지해줘."
            }
        }

    private fun buildStudioPrompt(
        subject: String,
        ageHint: String,
    ): String =
        """
        첨부된 사진 속 $subject 고급 전문 스튜디오 포트레이트 사진 스타일로 변환해줘.
        $ageHint
        배경은 완전한 흰색 또는 밝은 회색 무배경(seamless backdrop)으로 처리하고,
        왼쪽 45도 각도에서 대형 소프트박스 조명을 비춰 얼굴에 부드럽고 입체적인 음영을 만들어줘.
        85mm 인물 사진 렌즈 느낌으로 배경은 살짝 흐리게(얕은 심도) 처리하고,
        눈동자에는 자연스러운 캐치라이트가 반사되도록 해줘.
        피부는 자연스러운 질감을 유지하면서 깔끔하게 보정된 느낌으로,
        고급 패션 매거진이나 브랜드 화보 수준의 상업 사진 퀄리티로 표현해줘.
        인물의 얼굴형, 눈코입, 헤어스타일, 전체적인 생김새는 원본 사진과 최대한 동일하게 유지해줘.
        """.trimIndent()

    private fun buildZootopiaPrompt(
        subject: String,
        ageHint: String,
    ): String =
        """
        첨부된 사진 속 $subject 디즈니 애니메이션 쥬토피아(Zootopia) 스타일의 의인화된 동물 캐릭터로 변환해줘.
        $ageHint
        인물의 전체적인 분위기, 표정, 헤어스타일을 참고해서 가장 잘 어울리는 동물 종을 선택하고,
        쥬토피아 세계관에 맞는 세련되고 현대적인 의상을 입혀줘.
        배경은 다양한 동물들이 공존하는 활기차고 화려한 동물 도시(Zootopia 시내)로 설정하고,
        네온사인, 마천루, 분주한 거리 풍경을 담아줘.
        색감은 밝고 채도가 높으며 생동감 넘치는 디즈니 애니메이션 특유의 스타일로,
        캐릭터의 표정과 눈빛은 원본 인물의 분위기와 감정을 그대로 반영해줘.
        전체 퀄리티는 디즈니 공식 영화 장면 수준으로 렌더링해줘.
        """.trimIndent()

    private fun buildHanbokPrompt(
        subject: String,
        gender: Gender,
        ageRange: AgeRange,
    ): String {
        val costumeDesc = buildHanbokCostume(gender, ageRange)
        val hairDesc = buildHanbokHair(gender, ageRange)

        return """
            첨부된 사진 속 $subject 아름다운 한국 전통 한복을 입혀서 새롭게 그려줘.
            $costumeDesc
            $hairDesc
            배경은 조선시대 궁궐 정원으로, 만개한 벚꽃나무, 석등, 기와 처마, 연못을 배치해줘.
            조명은 따뜻한 황금빛 노을로 설정하고,
            전체적으로 고풍스럽고 우아한 한국 전통 회화 일러스트 스타일로 표현해줘.
            인물의 얼굴형과 생김새, 눈코입의 특징은 원본과 최대한 동일하게 유지해줘.
            """.trimIndent()
    }

    private fun buildHanbokCostume(
        gender: Gender,
        ageRange: AgeRange,
    ): String =
        when {
            gender == Gender.MALE && ageRange == AgeRange.AGE_0_7 -> {
                "남아 전통 돌한복 스타일로, 색동저고리와 풍성한 바지를 입히고 복건을 씌워줘."
            }

            gender == Gender.MALE && ageRange == AgeRange.AGE_8_13 -> {
                "남자 아이 전통 한복으로, 밝은 색 저고리와 바지를 입히고 복건을 씌워줘."
            }

            gender == Gender.MALE && ageRange == AgeRange.AGE_14_19 -> {
                "남자 청소년 전통 한복으로, 선명한 색상의 저고리와 바지를 입히고 머리에는 탕건을 씌워줘."
            }

            gender == Gender.MALE -> {
                "남성 전통 한복으로, 흰 두루마기에 갓을 쓴 선비 스타일로 표현하고 저고리는 자연스러운 색상으로 해줘."
            }

            gender == Gender.FEMALE && ageRange == AgeRange.AGE_0_7 -> {
                "여아 전통 돌한복 스타일로, 색동저고리와 풍성한 치마를 입히고 조바위를 씌워줘."
            }

            gender == Gender.FEMALE && ageRange == AgeRange.AGE_8_13 -> {
                "여자 아이 전통 한복으로, 선명한 색상의 저고리와 풍성한 치마를 입혀줘."
            }

            gender == Gender.FEMALE && ageRange == AgeRange.AGE_14_19 -> {
                "여자 청소년 전통 한복으로, 선명한 색상(붉은색, 연두색, 하늘색)의 저고리와 풍성한 실크 치마를 입혀줘."
            }

            else -> {
                "여성 전통 한복으로, 繊細한 꽃 자수가 있는 저고리와 풍성하게 흘러내리는 아이보리 또는 옥색 실크 치마를 입혀줘."
            }
        }

    private fun buildHanbokHair(
        gender: Gender,
        ageRange: AgeRange,
    ): String =
        when {
            gender == Gender.MALE -> {
                "머리는 전통 상투 또는 묶음 스타일로 자연스럽게 표현해줘."
            }

            ageRange == AgeRange.AGE_0_7 || ageRange == AgeRange.AGE_8_13 -> {
                "머리는 댕기를 드리운 양갈래 스타일로 귀엽게 꾸며줘."
            }

            ageRange == AgeRange.AGE_14_19 -> {
                "머리는 댕기를 드리운 단아한 스타일로 꾸며줘."
            }

            else -> {
                "머리는 전통 쪽머리 또는 댕기를 드리운 스타일로 꾸미고 비녀와 노리개도 추가해줘."
            }
        }

    private fun buildPixarPrompt(
        subject: String,
        ageHint: String,
    ): String =
        """
        첨부된 사진 속 $subject 픽사(Pixar) 스튜디오의 3D 애니메이션 캐릭터 스타일로 변환해줘.
        $ageHint
        눈은 실제보다 크고 둥글고 표현력 있게, 볼은 발그레하고 통통하게,
        전체적인 신체 비율은 픽사 특유의 살짝 과장된 귀여운 비율로 조정해줘.
        피부에는 픽사 특유의 SSS(피부 투광) 효과를 적용해 생동감 있게 표현하고,
        조명은 따뜻하고 부드러운 시네마틱 3점 조명으로 설정해줘.
        배경은 픽사 영화에 나올 법한 화려하고 디테일한 환경으로 구성하고,
        전체 퀄리티는 픽사 영화 공식 포스터 수준으로 렌더링해줘.
        인물의 헤어 색상, 얼굴 특징, 개성과 표정은 최대한 원본과 유사하게 유지해줘.
        """.trimIndent()

    private fun buildGhibliPrompt(
        subject: String,
        ageHint: String,
    ): String =
        """
        첨부된 사진 속 $subject 스튜디오 지브리(Studio Ghibli) 특유의 손그림 애니메이션 스타일로 다시 그려줘.
        $ageHint
        선은 부드럽고 자연스러운 손그림 느낌으로, 색은 수채물감처럼 따뜻하고 은은하게 표현해줘.
        배경은 지브리 영화에서 자주 등장하는 드넓은 초록 언덕, 고즈넉한 시골 마을, 또는 울창한 숲속으로 설정하고,
        바람에 살랑이는 풀잎, 흐드러진 꽃밭, 멀리 보이는 구름과 하늘을 세밀하게 묘사해줘.
        조명은 따뜻한 오후 햇살이 부드럽게 스며드는 느낌으로,
        전체적으로 평화롭고 따뜻하며 향수를 자극하는 지브리 특유의 감성을 담아줘.
        인물의 얼굴 생김새, 헤어스타일, 표정은 원본 사진을 기반으로 최대한 유사하게 유지해줘.
        """.trimIndent()

    private fun buildLightArtPrompt(
        subject: String,
        ageHint: String,
    ): String =
        """
        첨부된 사진 속 $subject 고급 파인아트 수채화(Watercolor) 일러스트 스타일로 변환해줘.
        $ageHint
        색감은 라벤더, 장밋빛, 연한 금색, 민트 등 부드러운 파스텔 톤을 중심으로,
        물감이 번지듯 자연스럽게 스며드는 워시(wash) 기법으로 표현해줘.
        윤곽선은 얇고 섬세한 잉크 선으로 처리하되, 일부는 의도적으로 흐릿하게 남겨줘.
        밝은 부분은 종이 흰색을 살려 투명하게, 어두운 부분은 색층을 겹쳐 깊이감을 표현해줘.
        배경은 꽃잎이나 빛 번짐 등 몽환적이고 감성적인 요소로 채워주고,
        전체적으로 갤러리에 걸릴 법한 섬세하고 아름다운 수채화 작품 느낌으로 완성해줘.
        인물의 얼굴 특징과 포즈는 원본 사진을 기반으로 유지해줘.
        """.trimIndent()
}
