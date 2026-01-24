package wiki.yumi.yumiwiki.common.exception

open class BaseException(
    val errorCode: BaseErrorCode
) : RuntimeException(errorCode.message)