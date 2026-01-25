package wiki.yumi.yumiwiki.common.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI 설정
 *
 * API 문서화를 위한 Swagger UI 설정을 제공한다.
 * production 환경에서는 자동으로 비활성화된다.
 */
@Configuration
@ConditionalOnProperty(
    name = ["springdoc.swagger-ui.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("YumiWiki API")
                    .description("""
                        주니어 개발자를 위한 한국어 기술 지식 위키

                        **슬로건**: 너랑 나랑 같이 만드는 지식 공방
                    """.trimIndent())
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("YumiWiki")
                            .url("https://api.yumi.wiki")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버"),
                    Server()
                        .url("https://api.yumi.wiki")
                        .description("운영 서버")
                )
            )
    }
}
