package wiki.yumi.yumiwiki.common.exception

import org.springframework.http.HttpStatus

interface BaseErrorCode {
    val status: HttpStatus
    val message: String
}
