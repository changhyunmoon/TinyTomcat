package connector.http11;

import connector.http11.dto.MappingData;
import container.TinyContext;
import container.TinyWrapper;
import java.util.HashMap;
import java.util.Map;

public class Http11Mapper {
    // [보관 형태] URI 경로(String)를 키로 하여 Wrapper를 저장합니다.
    private final Map<String, TinyWrapper> servletMappings = new HashMap<>();
    private TinyContext context;

    public void setContext(TinyContext context) {
        this.context = context;
    }

    // 컨테이너 초기화 시점에 서블릿들을 매퍼에 등록합니다.
    public void addWrapper(String path, TinyWrapper wrapper) {
        servletMappings.put(path, wrapper);
        System.out.println("[Mapper] Registered path: " + path);
    }

    public void map(String uri, MappingData mappingData) {
        // 1. 현재 컨텍스트 설정
        mappingData.context = this.context;

        // 2. 정확한 일치(Exact Match) 확인
        // 예: /hello 요청 -> /hello 서블릿 반환
        TinyWrapper wrapper = servletMappings.get(uri);

        // 3. (확장성) 만약 정확한 일치가 없다면 패턴 매칭을 시도할 수 있습니다.
        if (wrapper == null) {
            // 여기에 나중에 /user/* 같은 와일드카드나
            // .do 같은 확장자 매핑 로직을 추가할 수 있습니다.
        }

        mappingData.wrapper = wrapper;
    }
}