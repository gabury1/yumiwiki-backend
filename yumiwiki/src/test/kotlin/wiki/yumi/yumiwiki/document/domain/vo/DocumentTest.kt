package wiki.yumi.yumiwiki.document.domain.vo

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Document VO 테스트
 *
 * 마크다운 파싱, 메타데이터 추출, 위키링크 변환 등을 검증한다.
 * Document 클래스는 YumiWiki의 핵심 로직을 담당하므로 상세한 테스트가 필수적이다.
 */
@DisplayName("Document 클래스")
class DocumentTest {

    @Test
    fun `when document has metadata then parses correctly`() {
        // given: 메타데이터가 있는 마크다운
        val rawContent = """
            ---
            title: 문서 제목
            aliases: ["예시", "포맷", "샘플"]
            ---

            # 문서 제목

            테스트 내용입니다.
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: 메타데이터 파싱 성공
        assertNotNull(document.metadata)
        assertEquals("문서 제목", document.metadata?.title)
        assertEquals(3, document.metadata?.aliases?.size)
        assertTrue(document.metadata?.aliases?.contains("예시") == true)
    }

    @Test
    fun `when document has no metadata then returns null metadata`() {
        // given: 메타데이터가 없는 마크다운
        val rawContent = """
            # 제목

            내용입니다.
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: metadata는 null
        assertNull(document.metadata)
    }

    @Test
    fun `when document has invalid metadata then returns null metadata`() {
        // given: 잘못된 YAML 형식
        val rawContent = """
            ---
            invalid: yaml: syntax: error
            ---

            # 제목
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: metadata는 null (예외 발생하지 않음)
        assertNull(document.metadata)
    }

    @Test
    fun `when document has basic wiki link then converts to markdown link`() {
        // given: 기본 위키링크
        val rawContent = """
            ---
            title: 테스트
            aliases: []
            ---

            [[JavaScript]] 문서를 참고하세요.
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: 위키링크가 마크다운 링크로 변환
        assertTrue(document.body.contains("[JavaScript](/docs/JavaScript)"))
    }

    @Test
    fun `when document has alias wiki link then converts correctly`() {
        // given: 별칭이 있는 위키링크 [[문서|표시텍스트]]
        val rawContent = """
            ---
            title: 테스트
            aliases: []
            ---

            [[예시|example]] 링크입니다.
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: [[문서|표시텍스트]] → [표시텍스트](/docs/문서)
        assertTrue(document.body.contains("[example](/docs/예시)"))
    }

    @Test
    fun `when document has wiki links in code block then does not convert`() {
        // given: 코드블럭 안에 위키링크
        val rawContent = """
            ---
            title: 테스트
            aliases: []
            ---

            일반 텍스트의 [[JavaScript]] 링크는 변환됩니다.

            ```mermaid
            graph LR
                A[사용자] --> B[[서버 A]]
            ```

            또 다른 [[Python]] 링크.
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: 코드블럭 밖의 링크만 변환, 코드블럭 안은 그대로 유지
        assertTrue(document.body.contains("[JavaScript](/docs/JavaScript)"))
        assertTrue(document.body.contains("[[서버 A]]"))  // 코드블럭 안은 변환 안됨
        assertTrue(document.body.contains("[Python](/docs/Python)"))
    }

    @Test
    fun `when document has multiple code blocks then preserves all`() {
        // given: 여러 코드블럭
        val rawContent = """
            ---
            title: 테스트
            aliases: []
            ---

            ```javascript
            const [[test]] = "value";
            ```

            일반 텍스트 [[HTTP]]

            ```python
            [[variable]] = 123
            ```
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: 코드블럭은 그대로, 일반 텍스트만 변환
        assertTrue(document.body.contains("const [[test]] = \"value\";"))
        assertTrue(document.body.contains("[HTTP](/docs/HTTP)"))
        assertTrue(document.body.contains("[[variable]] = 123"))
    }

    @Test
    fun `when parsing example md then works correctly`() {
        // given: example.md와 유사한 구조
        val rawContent = """
            ---
            title: 문서 제목
            aliases: ["예시", "포맷", "샘플"]
            ---

            # 문서 제목

            여기는 해당 개념에 대한 정의와 개요를 작성하는 공간입니다. 중요한 키워드나 다른 문서로 연결되는 단어는 **[[JavaScript]]** 처럼 대괄호 두 개를 사용하여 감싸줍니다.

            - **기본 링크**: `[[JavaScript]]` (문서 제목 그대로 표시)
            - **별칭 링크**: `[[예시|example]]` (링크는 '예시' 문서로 연결되지만, 화면에는 'example'로 표시)

            ```mermaid
            graph LR
                A[사용자 요청] --> B{로드밸런서}
                B -- 성공 --> C[[서버 A]]
                B -- 실패 --> D[에러 페이지]
            ```
        """.trimIndent()

        // when: Document 객체 생성
        val document = Document(rawContent)

        // then: 메타데이터 파싱
        assertNotNull(document.metadata)
        assertEquals("문서 제목", document.metadata?.title)
        assertEquals(3, document.metadata?.aliases?.size)

        // then: 일반 텍스트의 위키링크 변환
        assertTrue(document.body.contains("[JavaScript](/docs/JavaScript)"))
        assertTrue(document.body.contains("[example](/docs/예시)"))

        // then: 코드블럭 안의 위키링크는 보존
        assertTrue(document.body.contains("C[[서버 A]]"))

        // then: 메타데이터는 body에서 제거됨
        assertTrue(!document.body.contains("title: 문서 제목"))
    }
}
