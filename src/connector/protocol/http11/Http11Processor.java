package connector.protocol.http11;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import container.TinyContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/*
    Request, Response 객체 생성,
    서블릿 컨테이너로 전달
 */
public class Http11Processor {

    public void process(InputStream in, OutputStream out, TinyContext context) {
        try {
            // Keep-Alive를 지원하기 위한 루프
            while (true) {
                // 1. HTTP/1.1 요청 파싱 (우리가 만든 Http11Request 객체 생성)
                // 만약 스트림에 읽을 데이터가 없다면 여기서 멈춤(Blocking)
                Http11Request request = new Http11Request(in);

                // 요청 라인이 비어있으면 클라이언트가 연결을 끊은 것으로 간주
                if (request.getMethod() == null) {
                    break;
                }

                System.out.println("[Http11Processor] Handling " + request.getMethod() + " " + request.getUri());

                // 2. 응답 객체 생성
                Http11Response response = new Http11Response(out);

                // 3. 서블릿 컨테이너(Context)로 전달하여 로직 실행 (Dispatch)
                context.dispatch(request, response);

                //실제 브라우저로 응답 데이터 전송
                response.finish();

                // 4. Keep-Alive 여부 확인
                // 헤더에 Connection: close가 있거나, Keep-Alive가 명시되지 않은 경우 루프 종료
                if (!request.isKeepAlive()) {
                    break;
                }

                // 주의: 다음 요청을 읽기 위해 입력 스트림에 데이터가 남아있는지 확인하거나
                // 브라우저가 다음 요청을 보낼 때까지 대기함
            }
        } catch (SocketException e) {
            // 클라이언트가 강제로 연결을 끊은 경우 (정상적인 상황)
            System.out.println("[Http11Processor] Client closed connection.");
        } catch (Exception e) {
            System.err.println("[Http11Processor] Error during processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}