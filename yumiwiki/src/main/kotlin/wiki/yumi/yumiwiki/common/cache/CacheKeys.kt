package wiki.yumi.yumiwiki.common.cache

/**
 * 캐시 이름 상수
 *
 * Spring Cache에서 사용하는 캐시 이름을 중앙에서 관리한다.
 * @Cacheable, @CacheEvict 등의 애노테이션에서 사용된다.
 */
object CacheKeys {
    /**
     * 문서 인덱스 캐시
     *
     * navigator.json 파일의 파싱 결과를 캐싱한다.
     * Key: "navigator"
     * Value: Map<String, List<String>> (문서명 -> 별칭 리스트)
     */
    const val DOC_INDEX = "docIndex"
}