package wiki.yumi.yumiwiki.common.log

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController @RequestMapping("/api")
class LogController (
    private val logAppender: LogAppender
)
{
    /**
     * 프론트엔드에서 전달된 커스텀 로그를 Loki에 기록한다.
     *
     * @param request [LogRequestDTO] 형태의 요청본문
     *
     * 요청본문 예시:
     * ```json
     * {
     *     "logType": "CUSTOM_LOG",
     *     "body": { "key": "value" }
     * }
     * ```
     */
    @CrossOrigin
    @PostMapping("/logs")
    fun postLog(@RequestBody request: LogRequestDTO): ResponseEntity<Unit> {
        logAppender.appendInfoLog(request.logType, request.body)
        return ResponseEntity.ok().build()
    }
}