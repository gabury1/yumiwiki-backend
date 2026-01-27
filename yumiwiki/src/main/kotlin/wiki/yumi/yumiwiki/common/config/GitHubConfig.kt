package wiki.yumi.yumiwiki.common.config

import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GitHubConfig {

    @Value("\${github.token:}")
    private lateinit var token: String

    @Bean
    fun gitHub(): GitHub {
        return if (token.isNotBlank()) {
            GitHubBuilder().withOAuthToken(token).build()
        } else {
            // 토큰이 없으면 익명 접근 (rate limit 제한 있음)
            GitHub.connectAnonymously()
        }
    }
}