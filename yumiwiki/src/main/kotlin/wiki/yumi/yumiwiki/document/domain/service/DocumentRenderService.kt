package wiki.yumi.yumiwiki.document.domain.service

import org.springframework.stereotype.Service
import wiki.yumi.yumiwiki.document.domain.vo.Document

/**
 * 문서 렌더링 서비스
 *
 * 원본 Document를 사용자에게 표시 가능한 형태로 변환한다.
 * 위키링크 변환, Infobox 파싱, 목차 생성 등 문서 렌더링과 관련된 모든 로직을 처리한다.
 *
 * @property documentIndexService 문서 존재 여부 확인을 위한 인덱스 서비스
 */
@Service
class DocumentRenderService(
    private val documentIndexService: DocumentIndexService
) {

    /**
     * 원본 Document를 렌더링하여 HTML로 변환한다.
     *
     * 현재 수행하는 작업:
     * - 위키링크를 HTML 링크로 변환 (빨간링크 판별 포함)
     * - 코드블럭 내부는 변환하지 않음
     *
     * 향후 추가 예정:
     * - Infobox 파싱
     * - 목차(TOC) 생성
     * - 기타 마크다운 확장 문법
     *
     * @param document 원본 문서
     * @return 렌더링된 HTML 본문
     */
    fun render(title:String, document: Document): String {
        var body = document.body

        // 위키링크 변환
        body = convertWikiLinks(title, body)

        // TODO: Infobox 파싱
        // TODO: 목차 생성

        return body
    }

    /**
     * 위키링크를 HTML 링크로 변환한다.
     *
     * 코드블럭 내부의 위키링크는 변환하지 않는다.
     * 문서가 존재하지 않으면 'wiki-link-nonexist' 클래스를 추가한다.
     *
     * 변환 규칙:
     * - `[[문서]]` → `<a href='/docs/문서' class='wiki-link-exist'>문서</a>`
     * - `[[문서|표시텍스트]]` → `<a href='/docs/문서' class='wiki-link-exist'>표시텍스트</a>`
     * - 존재하지 않는 문서 → `class='wiki-link-nonexist'`
     *
     * @param body 원본 본문
     * @return 위키링크가 변환된 본문
     */
    private fun convertWikiLinks(title: String, body: String): String {
        // 1. 코드블럭 추출 및 placeholder로 치환
        val codeBlockRegex = Regex("```[\\s\\S]*?```")
        val codeBlocks = mutableListOf<String>()
        var processedBody = body

        codeBlockRegex.findAll(body).forEachIndexed { index, match ->
            codeBlocks.add(match.value)
            processedBody = processedBody.replaceFirst(match.value, "{{CODE_BLOCK_$index}}")
        }

        // 2. 위키링크 변환 (코드블럭은 placeholder 상태)
        val wikiLinkRegex = Regex("\\[\\[(.+?)]]")
        processedBody = wikiLinkRegex.replace(processedBody) { match ->
            val content = match.groupValues[1]
            val (link, text) = if ("|" in content) {
                val split = content.split("|", limit = 2)
                split[0] to split[1]   // [[문서|표시텍스트]] → link=문서, text=표시텍스트
            } else {
                content to content      // [[문서]] → text = link = content
            }

            // 문서 존재 여부 확인 (캐시 조회)
            val exists = documentIndexService.searchDoc(link) != null
            val cssClass = if (exists) "wiki-link-exist" else "wiki-link-nonexist"

            "<a href='/docs/$link?referer_doc=$title' class='$cssClass'>$text</a>"
        }

        // 3. placeholder를 원본 코드블럭으로 복원
        codeBlocks.forEachIndexed { index, block ->
            processedBody = processedBody.replace("{{CODE_BLOCK_$index}}", block)
        }

        return processedBody
    }
}