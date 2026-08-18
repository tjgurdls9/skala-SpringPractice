package com.skala.stock.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 서버 기동이 완료되면 로컬 브라우저에서 Swagger UI를 자동으로 연다 (macOS 전용, open 명령 사용).
 * gradlew bootRun / IDE 실행 버튼 등 실행 방식과 무관하게 동작한다.
 */
@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            new ProcessBuilder("open", "http://localhost:8080/swagger-ui.html").start();
        } catch (Exception ignored) {
            // 브라우저를 열 수 없는 환경(예: 도커 컨테이너)에서는 조용히 무시한다.
        }
    }
}
