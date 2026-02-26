package connector.protocol.http20;

import connector.protocol.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Http20Response implements HttpResponse {
    private final OutputStream output;
    private final int streamId;
    private int statusCode = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];
    private boolean committed = false;

    public Http20Response(OutputStream output, int streamId) {
        this.output = output;
        this.streamId = streamId;
        // H2 응답의 기본 서버 정보
        setHeader("server", "tinytomcat/1.0");
    }

    @Override public void setStatus(int statusCode) { this.statusCode = statusCode; }
    @Override public void setHeader(String name, String value) { headers.put(name.toLowerCase(), value); }
    @Override public void addHeader(String name, String value) { headers.put(name.toLowerCase(), value); }
    @Override public void setContentType(String type) { setHeader("content-type", type); }
    @Override public void setBody(byte[] body) { this.body = (body != null) ? body : new byte[0]; }
    @Override public void setBody(String body) {
        if (body != null) this.body = body.getBytes(StandardCharsets.UTF_8);
    }

    @Override public boolean isCommitted() { return committed; }

    @Override
    public void send() throws IOException {
        if (committed) return;

        // 1. HEADERS 프레임 전송
        sendHeadersFrame();

        // 2. DATA 프레임 전송 (본문이 있을 경우)
        if (body.length > 0) {
            sendDataFrame();
        }

        output.flush();
        this.committed = true;
    }

    private void sendHeadersFrame() throws IOException {
        // 실제로는 여기서 HPACK 압축이 필요하지만, 구조 이해를 위해 비압축(Literal) 방식으로 가정
        ByteArrayOutputStream headerPayload = new ByteArrayOutputStream();

        // HTTP/2 필수 의사 헤더 :status
        writeHeaderField(headerPayload, ":status", String.valueOf(statusCode));

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            writeHeaderField(headerPayload, entry.getKey(), entry.getValue());
        }

        byte[] payload = headerPayload.toByteArray();
        // Type 0x01 (HEADERS), Flags 0x04 (END_HEADERS)
        writeFrameHeader(payload.length, 0x01, 0x04, streamId);
        output.write(payload);
    }

    private void sendDataFrame() throws IOException {
        // Type 0x00 (DATA), Flags 0x01 (END_STREAM)
        writeFrameHeader(body.length, 0x00, 0x01, streamId);
        output.write(body);
    }

    private void writeFrameHeader(int length, int type, int flags, int streamId) throws IOException {
        byte[] header = new byte[9];
        header[0] = (byte) ((length >> 16) & 0xFF);
        header[1] = (byte) ((length >> 8) & 0xFF);
        header[2] = (byte) (length & 0xFF);
        header[3] = (byte) (type & 0xFF);
        header[4] = (byte) (flags & 0xFF);
        header[5] = (byte) ((streamId >> 24) & 0x7F);
        header[6] = (byte) ((streamId >> 16) & 0xFF);
        header[7] = (byte) ((streamId >> 8) & 0xFF);
        header[8] = (byte) (streamId & 0xFF);
        output.write(header);
    }

    private void writeHeaderField(OutputStream os, String name, String value) throws IOException {
        // HPACK 없이 보낼 때의 아주 단순한 형태 (실제 규격과는 차이가 있음)
        os.write(0); // Literal Header Field without Indexing
        os.write(name.length());
        os.write(name.getBytes(StandardCharsets.UTF_8));
        os.write(value.length());
        os.write(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void finish() throws IOException {
        if (!committed) send();
    }
}