package mapping;

import java.util.ArrayList;

/*
    전체 컨테이너 계층 구조를 알고 있어야 하며, 특정 URI가 들어왔을 때 최적의 경로를 찾아준다.
    핵심 로직: String URI → MappingData (Host, Context, Wrapper 객체 묶음)

    MappingData.java (The Result)
    매퍼가 찾은 결과물입니다. Http11Processor는 이 객체를 보고
    "아, 이 요청은 A 호스트의 B 앱에 있는 C 서블릿이 처리해야 하는구나!"를 알게 된다

    Adapter.java (The Bridge)
    Tomcat의 CoyoteAdapter와 같은 역할입니다.
    Connector로부터 Request를 전달받아 Mapper를 통해 목적지를 찾고,
    실제 컨테이너의 invoke()를 호출합니다.
 */

public class Mapper {
    // 1. 모든 컨테이너 정보를 계층적으로 들고 있음
    private List<MappedHost> hosts = new ArrayList<>();

    // 2. 가장 중요한 메소드: URI를 넣으면 결과가 나옴
    public void map(String hostName, String uri, MappingData mappingData) {
        // (1) hostName으로 Host 찾기
        // (2) uri의 Context path(예: /api)로 Context 찾기
        // (3) 나머지 경로로 Wrapper(Servlet) 찾기
    }
}