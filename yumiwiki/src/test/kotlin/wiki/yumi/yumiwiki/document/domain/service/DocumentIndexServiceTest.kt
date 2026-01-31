package wiki.yumi.yumiwiki.document.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import com.github.benmanes.caffeine.cache.Caffeine
import wiki.yumi.yumiwiki.common.cache.CacheKeys
import wiki.yumi.yumiwiki.document.domain.port.out.DocumentLoader

class DocumentIndexServiceTest {

    private lateinit var documentLoader: DocumentLoader
    private lateinit var cacheManager: CacheManager
    private lateinit var documentIndexService: DocumentIndexService

    @BeforeEach
    fun setUp() {
        documentLoader = mockk()
        cacheManager = mockk()
        documentIndexService = DocumentIndexService(documentLoader, cacheManager)
    }

    @Test
    fun `refreshDocIndex should load and cache document mappings`() {
        // given
        val mockJson = """
        {
            "C Sharp": ["c#", "csharp", "씨샵"],
            "Java": ["java", "자바"],
            "Python": ["python", "py", "파이썬"]
        }
        """.trimIndent()

        val caffeineCache = CaffeineCache(
            CacheKeys.DOC_INDEX,
            Caffeine.newBuilder().build()
        )

        every { documentLoader.loadRootFile("navigator.json") } returns mockJson
        every { cacheManager.getCache(CacheKeys.DOC_INDEX) } returns caffeineCache

        // when
        val result = documentIndexService.refreshDocIndex()

        // then
        assertTrue(result)
        verify { documentLoader.loadRootFile("navigator.json") }
        verify { cacheManager.getCache(CacheKeys.DOC_INDEX) }

        // 캐시 내용 검증
        val cache = caffeineCache.nativeCache
        assertEquals("C Sharp", cache.getIfPresent("C Sharp"))
        assertEquals("C Sharp", cache.getIfPresent("c#"))
        assertEquals("C Sharp", cache.getIfPresent("csharp"))
        assertEquals("C Sharp", cache.getIfPresent("씨샵"))
        assertEquals("Java", cache.getIfPresent("Java"))
        assertEquals("Java", cache.getIfPresent("java"))
        assertEquals("Java", cache.getIfPresent("자바"))
        assertEquals("Python", cache.getIfPresent("Python"))
        assertEquals("Python", cache.getIfPresent("python"))
        assertEquals("Python", cache.getIfPresent("py"))
        assertEquals("Python", cache.getIfPresent("파이썬"))
    }

    @Test
    fun `refreshDocIndex should clear existing cache before loading`() {
        // given
        val mockJson = """
        {
            "C Sharp": ["c#"]
        }
        """.trimIndent()

        val caffeineCache = CaffeineCache(
            CacheKeys.DOC_INDEX,
            Caffeine.newBuilder().build()
        )

        // 기존 캐시에 데이터 추가
        caffeineCache.put("old-key", "old-value")

        every { documentLoader.loadRootFile("navigator.json") } returns mockJson
        every { cacheManager.getCache(CacheKeys.DOC_INDEX) } returns caffeineCache

        // when
        documentIndexService.refreshDocIndex()

        // then
        val cache = caffeineCache.nativeCache
        assertNull(cache.getIfPresent("old-key"))  // 기존 데이터 삭제됨
        assertEquals("C Sharp", cache.getIfPresent("C Sharp"))
        assertEquals("C Sharp", cache.getIfPresent("c#"))
    }

    @Test
    fun `readDocIndex should return all cached mappings`() {
        // given
        val mockJson = """
        {
            "C Sharp": ["c#", "csharp"],
            "Java": ["java"]
        }
        """.trimIndent()

        val caffeineCache = CaffeineCache(
            CacheKeys.DOC_INDEX,
            Caffeine.newBuilder().build()
        )

        every { documentLoader.loadRootFile("navigator.json") } returns mockJson
        every { cacheManager.getCache(CacheKeys.DOC_INDEX) } returns caffeineCache

        // when
        documentIndexService.refreshDocIndex()
        val result = documentIndexService.readDocIndex()

        // then
        assertEquals(5, result.size)  // "C Sharp", "c#", "csharp", "Java", "java"
        assertEquals("C Sharp", result["C Sharp"])
        assertEquals("C Sharp", result["c#"])
        assertEquals("C Sharp", result["csharp"])
        assertEquals("Java", result["Java"])
        assertEquals("Java", result["java"])
    }

    @Test
    fun `readDocIndex should return empty map when cache is null`() {
        // given
        every { cacheManager.getCache(CacheKeys.DOC_INDEX) } returns null

        // when
        val result = documentIndexService.readDocIndex()

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `readDocIndex should return empty map when cache is empty`() {
        // given
        val caffeineCache = CaffeineCache(
            CacheKeys.DOC_INDEX,
            Caffeine.newBuilder().build()
        )

        every { cacheManager.getCache(CacheKeys.DOC_INDEX) } returns caffeineCache

        // when
        val result = documentIndexService.readDocIndex()

        // then
        assertTrue(result.isEmpty())
    }
}