package wiki.yumi.yumiwiki.common.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * Caffeine 캐시 설정
 *
 * 문서 조회 성능 향상을 위한 로컬 캐시 설정을 제공한다.
 * GitHub API 호출을 최소화하여 응답 속도를 개선한다.
 */
@Configuration
@EnableCaching
class CacheConfig {

    /**
     * Caffeine 기반 캐시 매니저를 생성한다.
     *
     * Spring의 @Cacheable, @CacheEvict 등의 애노테이션을 처리하는 캐시 매니저다.
     * 내부적으로 Caffeine 캐시 엔진을 사용하여 고성능 로컬 캐싱을 제공한다.
     *
     * @return CacheManager 구현체
     */
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(caffeineCacheBuilder())
        return cacheManager
    }

    /**
     * Caffeine 캐시 설정을 구성한다.
     *
     * 주요 설정:
     * - maximumSize: 메모리에 보관할 최대 항목 수 (LRU 방식으로 제거)
     * - expireAfterWrite: 캐시 작성 후 만료 시간 (GitHub 콘텐츠 최신성 유지)
     * - recordStats: 캐시 히트율, 미스율 등 통계 수집 활성화
     *
     * 사용 예시:
     * - 문서 조회: GitHub API 호출 최소화
     * - 검색: 문서 목록 캐싱으로 검색 성능 향상
     *
     * @return Caffeine 빌더 인스턴스
     */
    private fun caffeineCacheBuilder(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .maximumSize(1000)  // 최대 1000개 항목 캐시 (약 1000개 문서 상당)
            .recordStats()  // 캐시 통계 기록 (모니터링/디버깅용, actuator로 확인 가능)
    }
}