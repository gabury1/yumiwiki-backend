package wiki.yumi.yumiwiki.common.exception.code

import org.springframework.http.HttpStatus
import wiki.yumi.yumiwiki.common.exception.BaseErrorCode

enum class CommonErrorCode(
    override val status: HttpStatus,
    override val message: String
) : BaseErrorCode {

    // ✅ 400 BAD REQUEST
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // ✅ 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // ✅ 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

    // ✅ 404 NOT FOUND
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");
}