package container;

import connector.protocol.HttpRequest;

/*

 */
public class Mapper {
    private final TinyContext context;

    public Mapper(TinyContext context) {
        this.context = context;
    }

    /**
     * 요청 객체에서 URI를 추출하고, 매핑된 Wrapper를 찾습니다.
     */
    public TinyWrapper map(HttpRequest request) {
        String uri = request.getRequestURI();

        // 1. URI 정규화 (Query String이나 세션 ID 등 제거)
        String cleanPath = normalize(uri);

        // 2. Context에 등록된 매핑 정보에서 Wrapper 검색
        return context.findWrapper(cleanPath);
    }


    private String normalize(String uri) {
        if (uri == null) return "/";

        String path = uri;

        // 1. 세션 아이디가 붙어있는 경우 처리 (';' 구분자 이후 제거)
        int semicolon = path.indexOf(';');
        if (semicolon >= 0) {
            path = path.substring(0, semicolon);
        }

        // 2. 쿼리 스트링 제거 ('?' 구분자 이후 제거)
        int question = path.indexOf('?');
        if (question >= 0) {
            path = path.substring(0, question);
        }

        // 3. 경로 끝의 '/' 제거 (단, 루트 "/" 자체는 유지)
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }
}