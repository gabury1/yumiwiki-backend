package wiki.yumi.yumiwiki.document.domain.service

import org.springframework.stereotype.Service
import wiki.yumi.yumiwiki.common.log.LogAppender

/**
 * 문서 도메인과 관련된 로그를 기록하는 서비스.
 *
 * [LogAppender]를 통해 구조화된 JSON 로그를 남으며, Loki에 수집된다.
 *
 * @see LogAppender
 */
@Service
class DocumentLogService (
    private val logAppender : LogAppender
)
{
    /**
     * 문서 조회 이벤트를 로그로 기록한다.
     *
     * `logType: DOCUMENT_VIEW_LOG`로 남기며, 기록되는 정보는 다음과 같다:
     * - `title` — 조회된 문서 제목
     * - `deviceId` — 클라이언트 장치 식별자 (`X-Device-ID` 헤더)
     *
     * @param title    조회된 문서 제목
     * @param deviceId 클라이언트 장치 식별자
     */
    fun appendDocumentViewLog(title : String, deviceId : String) {
        val documentViewLog : Map<String, String> = mapOf(
            "title" to title,
            "deviceId" to deviceId
        )
        logAppender.appendInfoLog("DOCUMENT_VIEW_LOG", documentViewLog)
    }

    /**
     * 외부 유입 출처를 로그로 기록한다.
     *
     * `utm_source` 파라미터가 있는 경우에만 호출되며,
     * `logType: DOCUMENT_UTM_LOG`로 남긴다.
     *
     * - `title` — 조회된 문서 제목
     * - `deviceId` — 클라이언트 장치 식별자
     * - `utmSource` — 외부 유입 출처 (예: `google`, `threads`)
     *
     * @param title     조회된 문서 제목
     * @param deviceId  클라이언트 장치 식별자
     * @param utmSource 외부 유입 출처 (`utm_source` 파라미터 값)
     */
    fun appendDocumentUtmLog(title : String, deviceId : String, utmSource : String) {
        val documentUtmLog : Map<String, String> = mapOf(
            "title" to title,
            "deviceId" to deviceId,
            "utmSource" to utmSource
        )
        logAppender.appendInfoLog("DOCUMENT_UTM_LOG", documentUtmLog)
    }

    /**
     * 내부 이동 출처를 로그로 기록한다.
     *
     * `referer_doc` 파라미터가 있는 경우에만 호출되며,
     * `logType: DOCUMENT_REFERER_LOG`로 남긴다.
     *
     * - `title` — 조회된 문서 제목
     * - `deviceId` — 클라이언트 장치 식별자
     * - `refererDoc` — 직전 문서 제목
     *
     * @param title      조회된 문서 제목
     * @param deviceId   클라이언트 장치 식별자
     * @param refererDoc 직전 문서 제목
     */
    fun appendDocumentRefererLog(title : String, deviceId : String, refererDoc : String) {
        val documentRefererLog : Map<String, String> = mapOf(
            "title" to title,
            "deviceId" to deviceId,
            "refererDoc" to refererDoc
        )
        logAppender.appendInfoLog("DOCUMENT_REFERER_LOG", documentRefererLog)
    }
}