package wiki.yumi.yumiwiki.document.application.dto.response

data class DocumentResponseDTO(
    val title : String?,
    val alias : List<String>?,
    val body : String
)
