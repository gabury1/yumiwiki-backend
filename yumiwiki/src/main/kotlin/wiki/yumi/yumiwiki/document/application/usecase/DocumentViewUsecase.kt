package wiki.yumi.yumiwiki.document.application.usecase

import org.springframework.stereotype.Service
import wiki.yumi.yumiwiki.document.application.dto.response.DocumentResponseDTO
import wiki.yumi.yumiwiki.document.domain.port.out.DocumentLoader
import wiki.yumi.yumiwiki.document.domain.service.DocumentRenderService
import wiki.yumi.yumiwiki.document.domain.vo.Document

/**
 * 문서 조회 유즈케이스
 *
 * 사용자가 특정 문서를 조회할 때 사용하는 애플리케이션 서비스다.
 * 문서 로더를 통해 문서를 가져오고, 클라이언트에게 반환할 DTO로 변환한다.
 *
 * @property documentLoader 문서를 로드하는 포트
 */
@Service
class DocumentViewUsecase(
    private val documentLoader: DocumentLoader,
    private val documentRenderService: DocumentRenderService
) {
    /**
     * 문서를 조회한다.
     *
     * 제목으로 문서를 검색하여 메타데이터와 본문을 반환한다.
     * 문서가 로컬 캐시에 없으면 GitHub에서 가져온다.
     *
     * @param title 조회할 문서 제목
     * @return 문서 정보를 담은 DTO (제목, 별칭, 본문)
     * @throws BaseException 문서를 찾을 수 없는 경우
     */
    fun documentView(title : String) : DocumentResponseDTO
    {
        // 문서를 로드
        val doc : Document = documentLoader.loadDocument(title)
        val body = documentRenderService.render(doc)

        return DocumentResponseDTO(
                doc.metadata?.title,
                doc.metadata?.aliases,
                body
        )
    }

}