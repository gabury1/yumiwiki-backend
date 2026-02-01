package wiki.yumi.yumiwiki.common.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import wiki.yumi.yumiwiki.common.log.LogAppender

/**
 * HTTP 요청/응답 액세스 로그를 기록하는 서블릿 필터.
 *
 * [OncePerRequestFilter]를 상속하여 요청당 한 번만 실행되며,
 * 모든 HTTP 요청에 대해 구조화된 JSON 로그를 [LogAppender]를 통해 남긴다.
 *
 * 기록되는 정보:
 * - `method` — HTTP 메서드 (GET, POST 등)
 * - `uri` — 요청 경로 + 쿼리스트링
 * - `status` — HTTP 응답 상태 코드
 * - `duration_ms` — 요청 처리 시간 (밀리초)
 * - `client_ip` — 클라이언트 IP (프록시 헤더 우선)
 * - `device_id` — 클라이언트 장치 식별자 (`X-Device-ID` 헤더)
 *
 * @see LogAppender
 * @see OncePerRequestFilter
 */
@Component
class AccessLogFilter(
    private val logAppender: LogAppender
) : OncePerRequestFilter() {

    /**
     * 요청을 래핑한 후 필터 체인을 실행하고, 완료 후 액세스 로그를 기록한다.
     *
     * 응답 본문이 정상적으로 클라이언트에 전달되도록 `finally` 블록에서
     * [ContentCachingResponseWrapper.copyBodyToResponse]를 호출한다.
     *
     * @param request  원본 HTTP 요청
     * @param response 원본 HTTP 응답
     * @param filterChain 다음 필터 체인
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logAccess(wrappedRequest, wrappedResponse, duration)
            wrappedResponse.copyBodyToResponse()
        }
    }

    /**
     * 요청/응답 정보를 수집하여 구조화된 액세스 로그를 남긴다.
     *
     * [LogAppender.appendInfoLog]를 통해 `logType: ACCESS_LOG`로 기록되며,
     * MDC에 자동으로 붙는 `profile` 컨텍스트와 함께 Loki에 수집된다.
     *
     * @param request  캐싱된 HTTP 요청 래퍼
     * @param response 캐싱된 HTTP 응답 래퍼
     * @param duration 요청 처리 시간 (밀리초)
     */
    private fun logAccess(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        duration: Long
    ) {
        val method = request.method ?: "unknown"
        val uri = request.requestURI ?: "unknown"
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val status = response.status
        val clientIp = getClientIp(request)

        val deviceId = request.getHeader("X-Device-ID") ?: "unknown"

        val accessLog = mapOf(
            "method" to method,
            "uri" to "$uri$queryString",
            "status" to status.toString(),
            "duration_ms" to duration.toString(),
            "client_ip" to clientIp,
            "device_id" to deviceId
        )

        logAppender.appendInfoLog("ACCESS_LOG", accessLog)
    }

    /**
     * 클라이언트의 실제 IP 주소를 반환한다.
     *
     * Cloudflare Tunnel 등 프록시 환경에서 원본 IP가 헤더로 전달되므로,
     * `X-Forwarded-For` → `X-Real-IP` → `remoteAddr` 순으로 우선순위를 둔다.
     *
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
     */
    private fun getClientIp(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")
            ?: request.getHeader("X-Real-IP")
            ?: request.remoteAddr
    }
}