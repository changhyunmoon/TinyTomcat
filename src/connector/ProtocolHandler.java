package connector;

/*
    네트워크 연결(소켓)과 애플리케이션 로직 사이를 이어주는 역할
    프로토콜 결정
    Processor 생성 및 할당
    커넥션 관리
    endpoint 와의 협업
 */

import java.net.Socket;

public interface ProtocolHandler {

    void handle(Socket socket);
}
