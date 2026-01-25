package wiki.yumi.yumiwiki.document.domain.port.out

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * DocumentLoader 인터페이스의 계약(contract)을 검증하는 테스트
 *
 * 실제 어댑터 구현체(GitHubDocumentAdapter 등)가 이 계약을 올바르게 준수하는지 확인한다.
 */
class DocumentLoaderTest {

    @Test
    fun `when loading document then returns content`() {
        // given: 테스트용 포트 구현체와 문서 이름
        val loader = TestDocumentLoader()
        val documentName = "test-document"

        // when: 문서 로드
        val content = loader.loadDocument(documentName)

        // then: 예상한 콘텐츠가 반환됨
        assertEquals("# Test Document\n\nThis is a test.", content)
    }

    @Test
    fun `when loading all documents then returns list of names`() {
        // given: 테스트용 포트 구현체
        val loader = TestDocumentLoader()

        // when: 모든 문서 조회
        val documentNames = loader.loadAllDocuments()

        // then: 문서 목록 반환
        assertEquals(2, documentNames.size)
        assertTrue(documentNames.contains("test-document"))
        assertTrue(documentNames.contains("another-doc"))
    }

    @Test
    fun `when loading root file then returns content`() {
        // given: 루트 파일 이름
        val loader = TestDocumentLoader()
        val fileName = "README"

        // when: 루트 파일 로드
        val content = loader.loadRootFile(fileName)

        // then: 루트 파일 콘텐츠 반환됨
        assertEquals("# README\n\nRoot file.", content)
    }

    @Test
    fun `when loading all root files then returns list of names`() {
        // given: 테스트용 포트 구현체
        val loader = TestDocumentLoader()

        // when: 모든 루트 파일 조회
        val fileNames = loader.loadAllRootFiles()

        // then: 루트 파일 목록 반환
        assertTrue(fileNames.contains("README"))
    }

    @Test
    fun `when loading non-existent document then throws exception`() {
        // given: 존재하지 않는 문서 이름
        val loader = TestDocumentLoader()
        val nonExistent = "non-existent"

        // when & then: 예외 발생 검증
        assertThrows<IllegalArgumentException> {
            loader.loadDocument(nonExistent)
        }
    }

    /**
     * 테스트용 DocumentLoader 구현체
     *
     * 실제 외부 시스템(GitHub 등)에 의존하지 않고 메모리 상의 데이터로 포트 계약을 검증한다.
     * 추후 MockK 등의 모킹 라이브러리로 대체 가능.
     */
    private class TestDocumentLoader : DocumentLoader {
        private val documents = mapOf(
            "test-document" to "# Test Document\n\nThis is a test.",
            "another-doc" to "# Another Document\n\nAnother content."
        )
        private val rootFiles = mapOf(
            "README" to "# README\n\nRoot file."
        )

        override fun loadDocument(name: String): String {
            return documents[name] ?: throw IllegalArgumentException("Document not found: $name")
        }

        override fun loadAllDocuments(): List<String> {
            return documents.keys.toList()
        }

        override fun loadRootFile(name: String): String {
            return rootFiles[name] ?: throw IllegalArgumentException("Root file not found: $name")
        }

        override fun loadAllRootFiles(): List<String> {
            return rootFiles.keys.toList()
        }
    }
}