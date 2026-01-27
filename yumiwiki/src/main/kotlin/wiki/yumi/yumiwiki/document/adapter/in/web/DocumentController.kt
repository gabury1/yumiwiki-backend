package wiki.yumi.yumiwiki.document.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import wiki.yumi.yumiwiki.document.application.dto.response.DocumentResponseDTO
import wiki.yumi.yumiwiki.document.application.usecase.DocumentViewUsecase

@Tag(name = "문서 API", description = "YumiWiki 문서 조회 API")
@RestController
@RequestMapping("/api")
class DocumentController(
    private val documentViewUsecase: DocumentViewUsecase
) {

    @Operation(
        summary = "문서 조회",
        description = "제목으로 문서를 검색하여 메타데이터와 본문을 반환합니다. 로컬 캐시에 없으면 GitHub에서 가져옵니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "문서 조회 성공",
                content = [Content(schema = Schema(implementation = DocumentResponseDTO::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "문서를 찾을 수 없음"
            )
        ]
    )
    @GetMapping("/docs/{name}")
    fun getDocs(
        @Parameter(description = "조회할 문서 제목 (예: HTTP, JavaScript)", required = true)
        @PathVariable("name") name: String
    ): ResponseEntity<Any> {

        val doc = documentViewUsecase.documentView(name)
        return ResponseEntity.ok().body(doc)

    }

}