package wiki.yumi.yumiwiki.document.domain.vo

import org.yaml.snakeyaml.Yaml

/**
 * 마크다운 문서를 표현하는 값 객체
 *
 * YAML 프론트 매터로 작성된 메타데이터와 본문을 파싱하여 위키링크를 마크다운 링크로 변환한다.
 *
 * @property rawContent 원본 마크다운 콘텐츠 (메타데이터 + 본문)
 * @property metadata 파싱된 메타데이터 (없거나 파싱 실패 시 null)
 * @property body 파싱된 본문 (메타데이터 제거, 위키링크 변환 완료)
 */
class Document(
    val rawContent: String
) {

    val metadata: Metadata? = parseMetadata(rawContent)
    val body: String = parseBody(rawContent)

    /**
     * 문서 메타데이터
     *
     * YAML 프론트 매터에서 추출한 제목과 별칭 정보를 담는다.
     *
     * @property title 문서 제목
     * @property aliases 문서의 별칭 목록 (검색 및 링크에 사용)
     */
    data class Metadata(
        val title: String,
        val aliases: List<String>
    )

    /**
     * YAML 프론트 매터에서 메타데이터를 파싱한다.
     *
     * 문서 상단의 `---`로 감싸진 YAML 블록을 추출하여 제목과 별칭 정보를 파싱한다.
     * 메타데이터가 없거나 YAML 형식이 잘못된 경우 null을 반환한다.
     *
     * @param raw 원본 마크다운 콘텐츠
     * @return 파싱된 메타데이터 또는 null
     */
    private fun parseMetadata(raw: String): Metadata? {
        // ---로 시작하고 두 번째 ---까지 추출
        val regex = Regex("---\\s*(.*?)\\s*---", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(raw) ?: return null  // 예외 대신 null 반환

        val yamlContent = match.groupValues[1]

        return try {
            val yaml = Yaml()
            val map = yaml.load<Map<String, Any>>(yamlContent)

            val title = map["title"] as? String ?: ""
            val aliases = (map["aliases"] as? List<*>)?.map { it.toString() } ?: emptyList()

            Metadata(title, aliases)
        } catch (e: Exception) {
            null  // YAML 파싱 실패 시에도 null 반환
        }
    }

    /**
     * 본문을 파싱하여 위키링크를 마크다운 링크로 변환한다.
     *
     * 메타데이터 블록을 제거하고, 위키 스타일 링크를 마크다운 링크로 변환한다.
     * 코드블럭 내부의 위키링크는 변환하지 않고 원본 그대로 유지한다.
     *
     * 변환 규칙:
     * - `[[문서]]` → `[문서](/docs/문서)`
     * - `[[문서|표시텍스트]]` → `[표시텍스트](/docs/문서)`
     * - 코드블럭(```) 내부는 변환하지 않음
     *
     * @param raw 원본 마크다운 콘텐츠
     * @return 메타데이터가 제거되고 위키링크가 변환된 본문
     */
    private fun parseBody(raw: String): String {
        val parts = raw.split("---")
        val bodyRaw = if (parts.size >= 3) parts.subList(2, parts.size).joinToString("---").trim()
        else ""

        // 1. 코드블럭 추출 및 placeholder로 치환
        val codeBlockRegex = Regex("```[\\s\\S]*?```")
        val codeBlocks = mutableListOf<String>()
        var processedBody = bodyRaw

        codeBlockRegex.findAll(bodyRaw).forEachIndexed { index, match ->
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
            "[$text](/docs/$link)"
        }

        // 3. placeholder를 원본 코드블럭으로 복원
        codeBlocks.forEachIndexed { index, block ->
            processedBody = processedBody.replace("{{CODE_BLOCK_$index}}", block)
        }

        return processedBody
    }
}