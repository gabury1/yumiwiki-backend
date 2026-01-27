package wiki.yumi.yumiwiki.document.adapter.out.github

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.test.util.ReflectionTestUtils
import wiki.yumi.yumiwiki.common.exception.BaseException
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * GitHubDocumentAdapter 테스트
 *
 * GitHub API를 모킹하여 어댑터가 DocumentLoader 계약을 올바르게 구현하는지 검증한다.
 * 실제 GitHub API 호출 없이 단위 테스트를 수행한다.
 */
class GitHubDocumentAdapterTest {

    @Test
    fun `when loading document then returns content from GitHub`() {
        // given: GitHub API 모킹
        val mockContent = mockk<GHContent>()
        every { mockContent.read() } returns ByteArrayInputStream("# Test Document\n\nContent from GitHub".toByteArray())

        val mockRepo = mockk<GHRepository>()
        every { mockRepo.getFileContent("docs/HTTP.md") } returns mockContent

        val mockGitHub = mockk<GitHub>()
        every { mockGitHub.getRepository("test-owner/test-repo") } returns mockRepo

        val adapter = createAdapter(mockGitHub)

        // when: 문서 로드
        val document = adapter.loadDocument("HTTP")

        // then: GitHub에서 가져온 콘텐츠 반환
        assertEquals("# Test Document\n\nContent from GitHub", document.rawContent)
    }

    @Test
    fun `when loading all documents then returns filtered md files`() {
        // given: 여러 파일이 있는 GitHub 저장소
        val mockContent1 = mockk<GHContent>()
        every { mockContent1.name } returns "doc1.md"

        val mockContent2 = mockk<GHContent>()
        every { mockContent2.name } returns "doc2.md"

        val mockContent3 = mockk<GHContent>()
        every { mockContent3.name } returns "README.txt"

        val mockRepo = mockk<GHRepository>()
        every { mockRepo.getDirectoryContent("docs") } returns listOf(mockContent1, mockContent2, mockContent3)

        val mockGitHub = mockk<GitHub>()
        every { mockGitHub.getRepository("test-owner/test-repo") } returns mockRepo

        val adapter = createAdapter(mockGitHub)

        // when: 모든 문서 조회
        val names = adapter.loadAllDocuments()

        // then: .md 파일만 필터링되어 확장자 없이 반환
        assertEquals(2, names.size)
        assertTrue(names.contains("doc1"))
        assertTrue(names.contains("doc2"))
    }

    @Test
    fun `when loading root file then returns content from GitHub`() {
        // given: 루트 디렉토리 파일 모킹
        val mockContent = mockk<GHContent>()
        every { mockContent.read() } returns ByteArrayInputStream("# README\n\nRoot file content".toByteArray())

        val mockRepo = mockk<GHRepository>()
        every { mockRepo.getFileContent("README.md") } returns mockContent

        val mockGitHub = mockk<GitHub>()
        every { mockGitHub.getRepository("test-owner/test-repo") } returns mockRepo

        val adapter = createAdapter(mockGitHub)

        // when: 루트 파일 로드
        val content = adapter.loadRootFile("README")

        // then: GitHub에서 가져온 콘텐츠 반환
        assertEquals("# README\n\nRoot file content", content)
    }

    @Test
    fun `when loading all root files then returns filtered md files`() {
        // given: 루트 디렉토리에 파일이 있는 GitHub 저장소
        val mockContent1 = mockk<GHContent>()
        every { mockContent1.name } returns "README.md"

        val mockContent2 = mockk<GHContent>()
        every { mockContent2.name } returns "CONTRIBUTING.md"

        val mockRepo = mockk<GHRepository>()
        every { mockRepo.getDirectoryContent("/") } returns listOf(mockContent1, mockContent2)

        val mockGitHub = mockk<GitHub>()
        every { mockGitHub.getRepository("test-owner/test-repo") } returns mockRepo

        val adapter = createAdapter(mockGitHub)

        // when: 루트 디렉토리의 모든 파일 조회
        val names = adapter.loadAllRootFiles()

        // then: .md 파일 반환
        assertEquals(2, names.size)
        assertTrue(names.contains("README"))
        assertTrue(names.contains("CONTRIBUTING"))
    }

    @Test
    fun `when configuration is invalid then throws IllegalStateException`() {
        // given: 설정이 비어있는 어댑터
        val mockGitHub = mockk<GitHub>()
        val adapter = GitHubDocumentAdapter(mockGitHub)
        ReflectionTestUtils.setField(adapter, "owner", "")
        ReflectionTestUtils.setField(adapter, "repositoryName", "")

        // when & then: 설정 검증 실패로 IllegalStateException 발생
        assertThrows<IllegalStateException> {
            adapter.loadDocument("test")
        }
    }

    @Test
    fun `when GitHub API fails then throws BaseException`() {
        // given: GitHub API 호출이 실패하는 상황
        val mockGitHub = mockk<GitHub>()
        every { mockGitHub.getRepository(any()) } throws RuntimeException("GitHub API error")

        val adapter = createAdapter(mockGitHub)

        // when & then: BaseException으로 래핑되어 발생
        assertThrows<BaseException> {
            adapter.loadDocument("test")
        }
    }

    /**
     * 설정이 주입된 어댑터 생성 (테스트용 헬퍼 메서드)
     */
    private fun createAdapter(gitHub: GitHub): GitHubDocumentAdapter {
        val adapter = GitHubDocumentAdapter(gitHub)
        ReflectionTestUtils.setField(adapter, "owner", "test-owner")
        ReflectionTestUtils.setField(adapter, "repositoryName", "test-repo")
        return adapter
    }
}