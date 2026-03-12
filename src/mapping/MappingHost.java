package mapping;

import java.util.concurrent.CopyOnWriteArrayList;

/*
    호스트 이름과 해당 호스트에 배포된 컨텍스트들을 관리한다.
 */
public class MappedHost {
    public final String name;
    public final TinyHost host;
    // 컨텍스트들을 저장 (탐색 효율을 위해 실제론 정렬된 배열이나 트리를 사용하지만, 여기선 List로 시작)
    public final List<MappedContext> contexts = new CopyOnWriteArrayList<>();

    public MappedHost(String name, TinyHost host) {
        this.name = name;
        this.host = host;
    }
    aa
}