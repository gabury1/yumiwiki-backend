package wiki.yumi.yumiwiki.common.exception.code

import org.springframework.http.HttpStatus
import wiki.yumi.yumiwiki.common.exception.BaseErrorCode

enum class DocumentErrorCode(
    override val status: HttpStatus,
    override val message: String
) : BaseErrorCode {
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다.")
}