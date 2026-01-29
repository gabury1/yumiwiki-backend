package wiki.yumi.yumiwiki.document.domain.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Cache
import jakarta.annotation.PostConstruct
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import wiki.yumi.yumiwiki.common.cache.CacheKeys
import wiki.yumi.yumiwiki.document.application.dto.response.DocumentListResponseDTO
import wiki.yumi.yumiwiki.document.domain.port.out.DocumentLoader

/**
 * 문서 검색 인덱스 관리 서비스
 *
 * navigator.json 파일을 로드하여 문서명과 별칭 간의 매핑을 캐시에 저장한다.
 * 이를 통해 사용자가 별칭으로 검색해도 실제 문서를 찾을 수 있다.
 */
@Service
class DocumentIndexService (
    private val documentLoader: DocumentLoader,
    private val cacheManager: CacheManager
)
{
    private val objectMapper = jacksonObjectMapper()

    @PostConstruct
    fun init() {
        // 최초 실행 시에 인덱스 생성
        refreshDocIndex()
    }

    /**
     * 문서명 또는 별칭으로 문서를 검색한다.
     *
     * @param query 검색어 (문서명 또는 별칭)
     * @return 실제 문서명 (없으면 null)
     * 예: "c#" -> "C Sharp", "Java" -> "Java"
     */
    fun searchDoc(query: String): String? {
        val cache = cacheManager.getCache(CacheKeys.DOC_INDEX) ?: return null
        return cache.get(query, String::class.java)
    }

    /**
     * 문서 목록을 반환한다.
     *
     * @param limit 반환할 문서 개수 (0이면 전체)
     * @return 문서 제목 목록
     */
    @Suppress("UNCHECKED_CAST")
    fun getDocList(limit: Int): DocumentListResponseDTO {
        val cache = cacheManager.getCache(CacheKeys.DOC_INDEX)
        val nativeCache = cache?.nativeCache as? Cache<String, Any>
        val list = nativeCache?.getIfPresent("docList") as? List<String> ?: emptyList()

        val shuffled = list.shuffled()
        val result = if (limit > 0) shuffled.take(limit) else shuffled
        return DocumentListResponseDTO(result)
    }


    /**
     * 문서 검색 인덱스를 갱신한다.
     *
     * 기존 캐시를 모두 삭제한 후, navigator.json 파일을 다시 로드하여
     * 문서명과 별칭 간의 매핑을 캐시에 저장한다.
     *
     * 생성되는 매핑:
     * - 문서명 -> 문서명 (예: "C Sharp" -> "C Sharp")
     * - 별칭 -> 문서명 (예: "c#" -> "C Sharp", "csharp" -> "C Sharp")
     *
     * @return 인덱스 갱신 성공 여부
     * @throws BaseException GitHub API 호출 실패 또는 JSON 파싱 실패 시
     */
    fun refreshDocIndex(): Boolean {
        val cache = cacheManager.getCache(CacheKeys.DOC_INDEX)

        // 기존 캐시 전체 삭제
        cache?.clear()

        val docJson = documentLoader.loadRootFile("navigator.json")
        val parseJson = objectMapper.readValue<Map<String, List<String>>>(docJson)

        // 문서 제목 리스트를 별도 저장
        cache?.put("docList", parseJson.keys.toList())

        // 문서명과 별칭을 캐시에 저장
        parseJson.forEach {
            val key = it.key
            cache?.put(key, key)
            it.value.forEach { alias ->
                cache?.put(alias, key)
            }
        }

        return true
    }

    /**
     * 캐시에 저장된 모든 매핑을 반환한다.
     *
     * @return 캐시에 저장된 모든 매핑 (키 -> 문서명)
     * 예: {"c#" -> "C Sharp", "csharp" -> "C Sharp", "C Sharp" -> "C Sharp"}
     */
    @Suppress("UNCHECKED_CAST")
    fun readDocIndex(): Map<String, String> {
        val cache = cacheManager.getCache(CacheKeys.DOC_INDEX) ?: return emptyMap()
        val nativeCache = cache.nativeCache as Cache<String, Any>
        return nativeCache.asMap()
            .filterKeys { it != "docList" }  // docList 키 제외
            .mapValues { it.value as String }
    }

}