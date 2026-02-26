package container.mapping;

import container.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Mapper {

    //저장된 모든 웹 애플리케이션(Context)들의 지도
    // Key: ContextPath (예: "/blog"), Value: Context 객체
    private final Map<String, Context> contexts = new ConcurrentHashMap<>();

    public void addContext(String contextPath, Context context){
        contexts.put(contextPath, context);
    }

    public Context map(String uri) {
        if (uri == null || uri.isEmpty()) {
            return contexts.get("/"); // 기본값
        }

        String bestMatch = null;

        // 1. 등록된 모든 ContextPath 중, 요청 URI의 시작 부분과 일치하는 것 찾기
        for (String contextPath : contexts.keySet()) {
            if (uri.startsWith(contextPath)) {
                // 2. 여러 개가 매칭될 경우, 가장 길게(구체적으로) 일치하는 것을 선택
                // 예: "/blog" vs "/blog/music" -> URI가 "/blog/music/list"라면 후자 선택
                if (bestMatch == null || contextPath.length() > bestMatch.length()) {
                    bestMatch = contextPath;
                }
            }
        }

        // 3. 만약 일치하는 게 전혀 없다면 루트("/") 컨텍스트를 반환하거나 null 반환
        return (bestMatch != null) ? contexts.get(bestMatch) : contexts.get("/");
    }

}
