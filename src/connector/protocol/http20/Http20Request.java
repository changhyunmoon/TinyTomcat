package connector.protocol.http20;

import connector.protocol.AbstractRequest;
import java.util.Map;

/*
    HTTP1.1 텍스트 기반 방식이 아닌 바이너리 프레이밍이라는 계층을 도입
    단순히 줄 단위가 아니라 프레임 이라는 조각으로 쪼개져서 온다

    프레임의 구조

 */

public class Http20Request extends AbstractRequest {

    private final int streamId;
    private boolean headersComplete = false;
    private boolean bodyComplete = false;

    /**
     * @param streamId 프레임 헤더에서 추출한 스트림 식별자
     * @param decodedHeaders HPACK 디코더를 통해 복원된 헤더 Map
     */
    public Http20Request(int streamId, Map<String, String> decodedHeaders) {
        this.streamId = streamId;
        this.protocol = "HTTP/2.0";

        // 1. HPACK으로 풀린 헤더들을 부모의 headers 맵에 주입
        this.headers.putAll(decodedHeaders);

        // 2. HTTP/2 의사 헤더(Pseudo-headers)를 표준 필드로 추출
        // HTTP/2는 :method, :path, :scheme, :authority 형식을 사용합니다.
        this.method = headers.get(":method");
        String fullPath = headers.get(":path");

        // 3. URI와 파라미터 분리 로직 (부모 클래스 메서드 활용)
        if (fullPath != null) {
            int questionMarkIndex = fullPath.indexOf('?');
            if (questionMarkIndex != -1) {
                this.uri = fullPath.substring(0, questionMarkIndex);
                // AbstractRequest의 파라미터 파싱 로직 호출
                super.parseParameters(fullPath.substring(questionMarkIndex + 1));
            } else {
                this.uri = fullPath;
            }
        }
    }

    // DATA 프레임이 올 때마다 바디 데이터를 누적하는 메서드
    public void appendBody(byte[] data) {
        if (this.body == null) {
            this.body = data;
        } else {
            byte[] combined = new byte[this.body.length + data.length];
            System.arraycopy(this.body, 0, combined, 0, this.body.length);
            System.arraycopy(data, 0, combined, this.body.length, data.length);
            this.body = combined;
        }
    }

    @Override
    public int getStreamId() {
        return this.streamId;
    }

    @Override
    public boolean isKeepAlive() {
        // HTTP/2는 기본적으로 모든 연결이 멀티플렉싱을 통한 지속 연결입니다.
        return true;
    }

    // 상태 관리용 (Processor가 사용)
    public void setHeadersComplete(boolean complete) { this.headersComplete = complete; }
    public boolean isHeadersComplete() { return headersComplete; }
    public void setBodyComplete(boolean complete) { this.bodyComplete = complete; }
    public boolean isBodyComplete() { return bodyComplete; }
}