package wiki.yumi.yumiwiki.document.domain.port.out

/**
 * 문서 로드를 위한 아웃바운드 포트
 *
 * 외부 저장소(GitHub 등)로부터 파일을 가져오는 인터페이스.
 * 구현체는 adapter.out 패키지에 위치한다.
 */
interface DocumentLoader {
    /**
     * docs 디렉토리의 문서 콘텐츠를 로드한다.
     *
     * @param name 문서 이름 (확장자 제외, 예: "HTTP")
     * @return 마크다운 원본 내용
     */
    fun loadDocument(name: String): String

    /**
     * docs 디렉토리의 모든 문서 이름 목록을 로드한다.
     *
     * @return 문서 이름 리스트 (확장자 제외)
     */
    fun loadAllDocuments(): List<String>

    /**
     * 루트 디렉토리의 파일 콘텐츠를 로드한다.
     *
     * @param name 파일 이름 (확장자 제외, 예: "README")
     * @return 파일 원본 내용
     */
    fun loadRootFile(name: String): String

    /**
     * 루트 디렉토리의 모든 마크다운 파일 이름 목록을 로드한다.
     *
     * @return 파일 이름 리스트 (확장자 제외)
     */
    fun loadAllRootFiles(): List<String>
}