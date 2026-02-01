package wiki.yumi.yumiwiki.common.log

/**
 * 프론트엔드에서 전달된 커스텀 로그 요청본문.
 *
 * @property logType 로그 타입 (예: `CUSTOM_LOG`)
 * @property body    키-값 맵 형태의 로그 본문
 */
data class LogRequestDTO(
    val logType: String,
    val body: Map<String, String>
)
