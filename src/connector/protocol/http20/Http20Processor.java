package connector.protocol.http20;

import container.TinyContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Http20Processor {

    // 하나의 연결 내에서 여러 요청이 동시에 진행되므로 스트림 ID별로 요청 객체를 관리
    private final Map<Integer, Http20Request> streamMap = new HashMap<>();

    public void process(InputStream in, OutputStream out, TinyContext context) throws Exception {
        // Connection Preface 읽기 (24바이트)
        // Handler에서 이미 확인했지만, 스트림에서 완전히 소모해야 다음 프레임을 읽을 수 있음
        byte[] preface = in.readNBytes(24);
        System.out.println("[Http20Processor] Connection Preface received.");

        while (true) {
            // 2. 9바이트 고정 헤더 읽기
            byte[] header = in.readNBytes(9);
            if (header.length < 9) break; // 연결 종료 시 루프 탈출

            // 3. 헤더 정보 파싱 (비트 연산)
            int length = ((header[0] & 0xFF) << 16) | ((header[1] & 0xFF) << 8) | (header[2] & 0xFF);
            int type = header[3] & 0xFF;
            int flags = header[4] & 0xFF;
            int streamId = ((header[5] & 0x7F) << 24) | ((header[6] & 0xFF) << 16) | ((header[7] & 0xFF) << 8) | (header[8] & 0xFF);

            // 4. 페이로드 읽기
            byte[] payload = in.readNBytes(length);

            // 5. 프레임 타입에 따른 핸들링
            handleFrame(type, flags, streamId, payload, out, context);
        }
    }

    private void handleFrame(int type, int flags, int streamId, byte[] payload, OutputStream out, TinyContext context) throws Exception {
        switch (type) {
            case 0x01: // HEADERS Frame
                handleHeadersFrame(flags, streamId, payload, out, context);
                break;

            case 0x00: // DATA Frame
                handleDataFrame(flags, streamId, payload, out, context);
                break;

            case 0x04: // SETTINGS Frame
                System.out.println("[Http20Processor] Received SETTINGS from stream " + streamId);
                break;

            default:
                System.out.println("[Http20Processor] Received Frame Type: " + type);
                break;
        }
    }

    private void handleHeadersFrame(int flags, int streamId, byte[] payload, OutputStream out, TinyContext context) throws Exception {
        // 실제로는 HPACK.decode(payload)가 필요함. 여기선 임시 Map으로 가정.
        Map<String, String> decodedHeaders = dummyHpackDecode(payload);

        Http20Request request = new Http20Request(streamId, decodedHeaders);
        streamMap.put(streamId, request);

        // END_STREAM 플래그가 설정되어 있다면 바디가 없는 GET 요청 등으로 간주하고 즉시 처리
        if ((flags & 0x01) != 0) {
            dispatchToContainer(request, out, context);
        }
    }

    private void handleDataFrame(int flags, int streamId, byte[] payload, OutputStream out, TinyContext context) throws Exception {
        Http20Request request = streamMap.get(streamId);
        if (request != null) {
            request.appendBody(payload);

            // END_STREAM 플래그가 설정되어 있다면 요청 수집 완료
            if ((flags & 0x01) != 0) {
                dispatchToContainer(request, out, context);
                streamMap.remove(streamId); // 처리 완료 후 맵에서 제거
            }
        }
    }

    private void dispatchToContainer(Http20Request request, OutputStream out, TinyContext context) throws Exception {
        System.out.println("[Http20Processor] Dispatching Stream " + request.getStreamId() + " to Container");
        Http20Response response = new Http20Response(out, request.getStreamId());
        context.dispatch(request, response);
        response.finish();
    }

    // HPACK 구현체 연결을 위한 더미 메서드
    private Map<String, String> dummyHpackDecode(byte[] payload) {
        // 실제 구현 시에는 HPACK 라이브러리나 직접 구현한 디코더를 사용해야 합니다.
        return Map.of(":method", "GET", ":path", "/hello", ":scheme", "http");
    }
}