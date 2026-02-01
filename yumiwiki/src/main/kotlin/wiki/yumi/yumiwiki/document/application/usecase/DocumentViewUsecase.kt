package wiki.yumi.yumiwiki.document.application.usecase

import org.springframework.stereotype.Service
import wiki.yumi.yumiwiki.common.exception.BaseException
import wiki.yumi.yumiwiki.document.application.dto.response.DocumentResponseDTO
import wiki.yumi.yumiwiki.document.domain.port.out.DocumentLoader
import wiki.yumi.yumiwiki.document.domain.service.DocumentIndexService
import wiki.yumi.yumiwiki.document.domain.service.DocumentLogService
import wiki.yumi.yumiwiki.document.domain.service.DocumentRenderService
import wiki.yumi.yumiwiki.document.domain.vo.Document
import wiki.yumi.yumiwiki.common.exception.code.DocumentErrorCode

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
    private val documentRenderService: DocumentRenderService,
    private val documentIndexService: DocumentIndexService,
    private val documentLogService: DocumentLogService
) {
    /**
     * 문서를 조회한다.
     *
     * 검색어로 문서를 검색하여 메타데이터와 렌더링된 본문을 반환한다.
     * 문서 인덱스에서 검색어에 해당하는 문서 제목(별칭 포함)을 찾고,
     * 해당 문서를 로드하여 HTML로 렌더링한다.
     * 조회 성공 후 [DocumentLogService]를 통해 로그를 남긴다.
     * - `DOCUMENT_VIEW_LOG` — 항상 남김
     * - `DOCUMENT_UTM_LOG` — `utmSource`가 있는 경우에만 남김
     * - `DOCUMENT_REFERER_LOG` — `refererDoc`가 있는 경우에만 남김
     *
     * @param query      검색어 (문서 제목 또는 별칭)
     * @param deviceId   클라이언트 장치 식별자 (`X-Device-ID` 헤더, nullable)
     * @param utmSource  외부 유입 출처 (`utm_source` 파라미터, nullable)
     * @param refererDoc 직전 문서 제목 (내부 이동 시 사용, nullable)
     * @return 문서 정보를 담은 DTO (제목, 별칭, 렌더링된 본문)
     * @throws BaseException 검색어에 해당하는 문서를 찾을 수 없는 경우 (DOCUMENT_NOT_FOUND)
     */
    fun documentView(query : String, deviceId: String?, utmSource: String?, refererDoc: String?) : DocumentResponseDTO
    {
        // 별칭까지 포함하여 검색
        val title = documentIndexService.searchDoc(query)
            ?: throw BaseException(DocumentErrorCode.DOCUMENT_NOT_FOUND)

        // 문서를 로드
        val doc : Document = documentLoader.loadDocument(title)
        val body = documentRenderService.render(title, doc)

        // 문서 뷰 로그
        val resolvedDeviceId = deviceId ?: "unknown"
        documentLogService.appendDocumentViewLog(title = title, deviceId = resolvedDeviceId)
        utmSource?.let { documentLogService.appendDocumentUtmLog(title = title, deviceId = resolvedDeviceId, utmSource = it) }
        refererDoc?.let { documentLogService.appendDocumentRefererLog(title = title, deviceId = resolvedDeviceId, refererDoc = it) }

        return DocumentResponseDTO(
                doc.metadata?.title,
                doc.metadata?.aliases,
                body
        )
    }

}