# 백엔드 4일차 실습과제 — 주식 거래 시스템 (기능 추가 개발)

Spring Boot 3.2 / Java 21 / H2(in-memory) / **JPA + MyBatis(SQL Mapper) 병행** / Actuator / AOP

```bash
./gradlew bootRun        # http://localhost:8080
./gradlew test           # 통합 테스트 8건
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:stockdb`, user `sa`)

---

## 1. 설계 요약 — JPA 와 SQL Mapper 의 역할 분리

| 계층 | 기술 | 이유 |
|---|---|---|
| CRUD (User / Stock / Portfolio / Transaction) | **JPA (Spring Data JPA)** | 단일 애그리거트 단위 CUD. 더티 체킹·영속성 컨텍스트 이점을 그대로 사용 |
| 분석/집계 조회 (`/api/analysis/**`) | **MyBatis (SQL Mapper)** | 3~4개 테이블 조인 + `SUM`/`CASE WHEN`/`GROUP BY` 집계. JPQL 로 표현하면 오히려 복잡해지고 N+1 위험이 있어 SQL 을 직접 작성 |

SQL 은 `src/main/resources/mapper/StockMapper.xml`, 인터페이스는 `com.skala.stock.mapper.StockMapper` 에 있다.

초기 데이터(`src/main/resources/data.sql`): **주식 12건**, 사용자 3명, 포트폴리오 3건, 거래 4건
(분석 API 를 바로 확인할 수 있도록 거래 내역은 최근 2일치로 흩어 놓았다).

---

## 2. 기본 CRUD API

| # | 기능 | 메서드 | 경로 |
|---|---|---|---|
| 1 | User 삭제 | `DELETE` | `/api/users/{id}` |
| 2 | User 전체 조회 | `GET` | `/api/users` |
| 3 | Stock 수정 | `PUT` | `/api/stocks/{id}` |
| 4 | Stock 삭제 | `DELETE` | `/api/stocks/{id}` |
| 5 | Stock 코드로 조회 | `GET` | `/api/stocks/code/{code}` |
| 6 | Transaction 상세 조회 (Read-Only) | `GET` | `/api/transactions/{id}` |
| 7 | 주식 매매 실행 | `POST` | `/api/transactions/trade` |
| 8 | Portfolio 특정 주식 조회 | `GET` | `/api/portfolios/user/{userId}/stock/{stockId}` |
| + | Transaction 특정 주식 거래 내역 | `GET` | `/api/transactions/user/{userId}/stock/{stockId}` |

구현하면서 챙긴 것

- **User 삭제**: `portfolios`/`transactions` 가 `users` 를 FK 로 참조하므로 자식 데이터를 먼저 정리한 뒤 삭제한다. 세 번의 삭제를 하나의 `@Transactional` 로 묶었다.
- **Stock 삭제**: 보유자나 거래 이력이 있으면 참조 무결성이 깨지므로 `400` 으로 막는다.
- **Stock 수정**: `previousPrice` 를 생략하고 현재가만 바꾸면, 기존 현재가를 전일 종가로 자동 이월한다.
- **매매 실행**: 잔액 검증 → 잔액 차감/증가 → 포트폴리오 갱신(평균 매수가 재계산) → 거래 이력 저장을 한 트랜잭션으로 묶는다. 중간에 실패하면 전부 롤백된다.

```bash
# 매수
curl -X POST localhost:8080/api/transactions/trade -H 'Content-Type: application/json' \
  -d '{"userId":1,"stockId":4,"type":"BUY","quantity":4}'

# 잔액 부족 → 400
# {"status":400,"message":"잔액이 부족합니다. 필요 금액: 78000000, 보유 금액: 3000000", ...}
```

---

## 3. 분석/고급 기능 API (요구 5개 이상 → **8개 전부 + 확장 3개**)

전부 `StockAnalysisController` / `StockAnalysisService` / `StockMapper`(MyBatis) 로 구현했다.

| # | 요구 항목 | 경로 |
|---|---|---|
| 1 | 포트폴리오 평가 손익 조회 | `GET /api/analysis/portfolio/{userId}` |
| 2 | 거래 내역 상세 조회 | `GET /api/analysis/transactions/{userId}` |
| 3 | 특정 주식 거래 내역 조회 | `GET /api/analysis/transactions/{userId}/stock/{stockId}` |
| 4 | 총 자산 조회 | `GET /api/analysis/assets/{userId}` |
| 5 | 총 수익률 조회 | `GET /api/analysis/return-rate/{userId}` |
| 6 | 거래 통계 조회 | `GET /api/analysis/statistics/stock/{stockId}` |
| 7 | 일별 거래 내역 조회 | `GET /api/analysis/transactions/{userId}/daily?date=2026-08-04` |
| 8 | Transaction 특정 주식 거래 내역 (JPA) | `GET /api/transactions/user/{userId}/stock/{stockId}` |

추가로 넣은 확장 조회

| 확장 | 경로 |
|---|---|
| 특정 주식 평가 손익 | `GET /api/analysis/portfolio/{userId}/stock/{stockId}` |
| 거래 단건 상세 (조인 버전) | `GET /api/analysis/transaction/{id}` |
| 자산 종합 요약 (총자산+수익률 한 번에) | `GET /api/analysis/summary/{userId}` |
| 사용자 기준 종목별 거래 통계 | `GET /api/analysis/statistics/user/{userId}` |
| 일자별 거래 집계 | `GET /api/analysis/transactions/{userId}/daily-summary` |

```bash
$ curl -s localhost:8080/api/analysis/summary/1
{"userId":1,"username":"user1","cashBalance":1734000,"stockValue":1300000,
 "totalAssets":3034000,"investedAmount":1290000,"evaluationProfitLoss":10000,
 "totalReturnRate":0.78}
```

SQL 작성 시 주의한 점

- **정수 나눗셈**: 수익률 계산에서 `BIGINT / BIGINT` 는 소수점이 잘려 0 이 되므로 `CAST(... AS DOUBLE)` 후 나눈다.
- **날짜 비교**: `DATE(col)` 대신 표준 `CAST(col AS DATE)` 를 써서 H2 에서 안전하게 동작시킨다.
- 반복되는 조인 컬럼 목록은 `<sql id="transactionDetailColumns">` 로 묶어 재사용한다.

---

## 4. AOP 적용

`com.skala.stock.aop` 패키지에 두 개의 Aspect 를 뒀다.

### 4-1. `ExecutionTimeAspect` — 실행 시간 측정/로깅

컨트롤러·서비스 전 메서드에 `@Around` 로 걸어 실행 시간을 남긴다. 200ms 이상이면 `WARN`, 예외가 나면 `[FAIL]` 로 기록한다.

```
DEBUG [TIME] StockService.getStockByCode - 12ms
WARN  [FAIL] StockService.getStockByCode - 1ms (ResourceNotFoundException: 주식을 찾을 수 없습니다. 종목 코드: 999999)
```

### 4-2. `TradeAuditAspect` — 거래 감사 로그 + 메트릭

매매 성공/실패 기록은 매매 로직의 본질이 아니라서 Aspect 로 분리했다. `TransactionService.executeTrade()` 는 감사 로그의 존재를 전혀 모른다.

두 가지 포인트가 있다.

1. **`@Order(HIGHEST_PRECEDENCE)`** — 트랜잭션 AOP(`@Transactional`, 기본 `LOWEST_PRECEDENCE`)보다 **바깥**에서 돌아야 `@AfterReturning` 시점에 커밋이 끝난 상태의 자산 스냅샷을 읽을 수 있다.
2. **`REQUIRES_NEW`** — `TradeAuditService.record()` 가 별도 트랜잭션을 열기 때문에, 거래가 롤백되는 실패 케이스에서도 "왜 실패했는지"가 남는다.

```bash
$ curl -s localhost:8080/actuator/tradeaudit
{"totalCount":4,"recent":[
  {"id":4,"userId":1,"type":"SELL","message":"[실패] SELL 5주 - 보유 수량이 부족합니다. 보유 수량: 0, 매도 수량: 5", ...},
  {"id":2,"userId":1,"type":"SELL","message":"[성공] SELL 10주 @70000 = 700000원 (거래 ID: 6)","totalAssets":3034000, ...}]}
```

---

## 5. Actuator 적용

`management.endpoints.web.exposure.include` 로 `health, info, metrics, env, beans, mappings, loggers, httpexchanges, tradeaudit` 를 노출한다. (학습용 설정이며, 운영에서는 최소 노출 + 인증이 필요하다.)

| 엔드포인트 | 내용 |
|---|---|
| `/actuator/health` | 기본 DB/디스크 + **커스텀 `stockMarket`** |
| `/actuator/info` | 애플리케이션 메타 정보 (`application.yml` 의 `info.*`) |
| `/actuator/metrics/stock.trade.count` | **커스텀 메트릭** — AOP 에서 올리는 매매 성공/실패 카운터 |
| `/actuator/tradeaudit` | **커스텀 엔드포인트** — AOP 가 남긴 거래 감사 로그 |
| `/actuator/httpexchanges` | 최근 HTTP 요청 (`InMemoryHttpExchangeRepository` 빈 등록) |

```bash
$ curl -s localhost:8080/actuator/health
{"status":"UP","components":{ ...,
  "stockMarket":{"status":"UP","details":{"stockCount":12,"userCount":3,"reason":"거래 가능"}}}}

$ curl -s localhost:8080/actuator/metrics/stock.trade.count
{"name":"stock.trade.count","measurements":[{"statistic":"COUNT","value":4.0}],
 "availableTags":[{"tag":"result","values":["success","failure"]}]}
```

`StockMarketHealthIndicator` 는 DB 커넥션이 살아 있어도 **종목 데이터가 비어 있으면 매매가 불가능**하므로 `DOWN` 으로 판정한다.

---

## 6. 예외 처리

`RuntimeException` 을 그대로 던지던 코드를 의미 있는 예외로 바꾸고, `@RestControllerAdvice` 에서 HTTP 상태로 매핑한다.

| 예외 | 상태 | 예 |
|---|---|---|
| `ResourceNotFoundException` | `404` | 없는 사용자/종목/거래 조회 |
| `BusinessException` | `400` | 잔액 부족, 보유 수량 부족, 중복 종목 코드, 참조 중인 종목 삭제 |
| `MethodArgumentNotValidException` | `400` | `fieldErrors` 에 필드별 메시지 |

```json
{
  "timestamp": "2026-08-06T16:34:33.630227",
  "status": 400, "error": "Bad Request",
  "message": "요청 값 검증에 실패했습니다",
  "path": "/api/transactions/trade",
  "fieldErrors": {"quantity": "거래 수량은 필수입니다", "type": "거래 유형은 필수입니다"}
}
```

---

## 7. 패키지 구조

```
com.skala.stock
├── actuator/    StockMarketHealthIndicator, TradeAuditEndpoint
├── aop/         ExecutionTimeAspect, TradeAuditAspect
├── config/      ActuatorConfig
├── controller/  User, Stock, Transaction, Portfolio, StockAnalysis
├── dto/         UserDto, StockDto, PortfolioDto, TransactionDto,
│                TradeRequestDto, TradeSnapshotDto, DailyTransactionSummaryDto
├── entity/      User, Stock, Portfolio, Transaction, TradeAuditLog
├── exception/   GlobalExceptionHandler, ResourceNotFoundException, BusinessException, ErrorResponse
├── mapper/      StockMapper (MyBatis), TransactionStatisticsDto
├── repository/  JPA Repository 5종
└── service/     User, Stock, Portfolio, Transaction, StockAnalysis, TradeAudit
```

## 8. 테스트

`src/test/java/com/skala/stock/StockTradingIntegrationTest.java` — MockMvc 기반 통합 테스트 8건.
초기 데이터 적재, 코드 조회, 매수 반영, 매도 실패 시 **롤백되어도 감사 로그는 남는지**, 검증 오류 응답,
MyBatis 집계 결과(총자산/수익률/거래통계), Actuator 커스텀 health·endpoint 를 검증한다.
