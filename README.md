# spring-practice

SKALA Spring Boot 백엔드 실습 모음. 기초 예제부터 개인 과제 프로젝트까지 일자별로 쌓았습니다.

각 폴더가 독립된 Gradle 프로젝트라 해당 폴더에서 `./gradlew bootRun`으로 따로 실행합니다.

## 프로젝트

| 폴더 | 내용 |
|---|---|
| `demo` | Spring Boot 기본 구조, Profile별 Config 분기 (`DevConfig` · `ProdConfig`) |
| `configuration-profile-sample` | Configuration/Profile 예제. `@ConfigurationProperties`와 프로필별 Storage 구현 교체 (Local ↔ Cloud) |
| `springboot-menu-recommendation` | "오늘 뭐 먹지?" — Component Scan · Bean 등록 · Spring Container 학습용 |
| `StockTrading` | 2일차 주식 거래 시스템. User · Stock · Transaction · Portfolio CRUD, Docker 이미지화 |
| `StockTrading2` | 4일차 기능 추가 개발. JPA + MyBatis 병행, Actuator, AOP |
| `G013_서혁인` | 개인 과제 — 팀 매칭 플랫폼 TeamMatch |

## G013_서혁인 — TeamMatch

프로젝트 팀원 모집·지원·수락을 처리하는 플랫폼입니다.

- **모집** — 프로젝트별 기술스택(`ProjectTechStack`)과 역할별 모집 슬롯(`RecruitSlot` · `RecruitRole`) 관리
- **지원** — 지원서 제출과 팀장 승인/거절(`TeamApplication`), 수락 시 채팅방(`ChatRoom`) 자동 개설
- **감사 로그** — AOP(`DecisionAuditAspect`)로 승인/거절 결정을 `ApplicationDecisionLog`에 자동 기록
- **통계** — MyBatis(`StatisticsMapper.xml`)로 기술스택 수요·사용자 랭킹 집계. JPA와 병행
- **파일 저장** — `StorageService` 인터페이스로 로컬/S3 구현을 프로필에 따라 교체 (`prod`는 실제 AWS 자격증명 없이 동작하는 시뮬레이션)
- **모니터링** — 커스텀 Actuator 지표 `ActiveProjectHealthIndicator`

동작 화면은 `G013_서혁인/captures/`, 상세 설명은 `보고서.html`에 있습니다.

## 실행

```bash
cd <프로젝트 폴더>
./gradlew bootRun    # http://localhost:8080
```

DB는 전부 H2 인메모리(`sa` / 비밀번호 없음)라 별도 설치 없이 바로 뜹니다. 재시작하면 데이터는 초기화되고 `data.sql`로 다시 채워집니다.

`StockTrading`은 Docker로도 실행할 수 있습니다.

```bash
cd StockTrading
docker build -t stock-trading .
docker run -p 8080:8080 stock-trading
```

## 환경

JDK 21 · Spring Boot 3.2 · Gradle Wrapper 동봉
