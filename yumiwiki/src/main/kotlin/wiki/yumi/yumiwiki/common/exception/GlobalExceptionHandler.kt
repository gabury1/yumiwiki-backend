package wiki.yumi.yumiwiki.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    // BaseException 처리
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException, request: HttpServletRequest): ResponseEntity<Any> {
        return ResponseEntity.status(ex.errorCode.status).body(
            mapOf(
                "status" to ex.errorCode.status,
                "message" to ex.errorCode.message,
                "path" to "${request.method} ${request.requestURI}"
            )
        )
    }

    // RuntimeException 처리
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: HttpServletRequest): ResponseEntity<Any> {
        return ResponseEntity.status(500).body(
            mapOf(
                "status" to 500,
                "error" to "Internal Server Error",
                "message" to ex.message,
                "path" to "${request.method} ${request.requestURI}",
                "timestamp" to LocalDateTime.now()
            )
        )
    }
}
