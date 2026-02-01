package wiki.yumi.yumiwiki.common.log

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service


@Service
class LogAppender(
    private val objectMapper: ObjectMapper,
    @Value("\${spring.profiles.active:default}")
    private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(LogAppender::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        // MDC에 profile 설정 - 모든 로그에 자동으로 포함됨
        MDC.put("profile", activeProfile)

        val startupLog = mapOf(
            "event" to "server_started",
            "application" to "yumiwiki-backend"
        )

        appendInfoLog("SERVER_START_LOG", startupLog)
    }

    fun appendInfoLog(logType:String, infoLog: Map<String, String>) {
        MDC.put("logType", logType)
        logger.info(objectMapper.writeValueAsString(infoLog))
        MDC.remove("logType")
    }

    fun appendErrorLog(logType:String, errorLog: Map<String, String>) {
        MDC.put("logType", logType)
        logger.error(objectMapper.writeValueAsString(errorLog))
        MDC.remove("logType")
    }

}
