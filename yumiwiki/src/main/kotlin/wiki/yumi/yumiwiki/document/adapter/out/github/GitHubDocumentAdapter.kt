package wiki.yumi.yumiwiki.document.adapter.out.github

import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import wiki.yumi.yumiwiki.common.exception.BaseException
import wiki.yumi.yumiwiki.common.exception.code.CommonErrorCode
import wiki.yumi.yumiwiki.document.domain.port.out.DocumentLoader

/**
 * GitHub 저장소로부터 파일을 로드하는 어댑터
 *
 * DocumentLoader를 구현하여 GitHub API를 통해 파일을 가져온다.
 */
@Component
class GitHubDocumentAdapter(
    private val gitHub: GitHub
) : DocumentLoader {

    @Value("\${github.repository.owner:}")
    private lateinit var owner: String

    @Value("\${github.repository.name:}")
    private lateinit var repositoryName: String

    override fun loadDocument(name: String): String {
        validateConfiguration()

        try {
            val repository = gitHub.getRepository("$owner/$repositoryName")
            val content = repository.getFileContent("docs/$name.md")
            return content.read().use { it.readBytes().toString(Charsets.UTF_8) }
        } catch (e: Exception) {
            throw BaseException(CommonErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    override fun loadAllDocuments(): List<String> {
        validateConfiguration()

        try {
            val repository = gitHub.getRepository("$owner/$repositoryName")
            val contents = repository.getDirectoryContent("docs")

            return contents
                .filter { it.name.endsWith(".md") }
                .map { it.name.removeSuffix(".md") }
        } catch (e: Exception) {
            throw BaseException(CommonErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    override fun loadRootFile(name: String): String {
        validateConfiguration()

        try {
            val repository = gitHub.getRepository("$owner/$repositoryName")
            val content = repository.getFileContent("$name.md")
            return content.read().use { it.readBytes().toString(Charsets.UTF_8) }
        } catch (e: Exception) {
            throw BaseException(CommonErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    override fun loadAllRootFiles(): List<String> {
        validateConfiguration()

        try {
            val repository = gitHub.getRepository("$owner/$repositoryName")
            val contents = repository.getDirectoryContent("/")

            return contents
                .filter { it.name.endsWith(".md") }
                .map { it.name.removeSuffix(".md") }
        } catch (e: Exception) {
            throw BaseException(CommonErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    /**
     * GitHub 저장소 설정이 올바른지 검증한다.
     *
     * @throws IllegalStateException owner 또는 repositoryName이 비어있는 경우
     */
    private fun validateConfiguration() {
        if (owner.isBlank() || repositoryName.isBlank()) {
            throw IllegalStateException(
                "GitHub 저장소 설정이 필요합니다. " +
                "application.yaml에 github.repository.owner와 github.repository.name을 설정해주세요."
            )
        }
    }
}