package com.skala.stock.aop;

import com.skala.stock.dto.TradeRequestDto;
import com.skala.stock.dto.TransactionDto;
import com.skala.stock.service.TradeAuditService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 거래 감사 로그 AOP.
 *
 * 매매 성공/실패 기록은 매매 로직의 본질이 아니라서 Aspect 로 분리했다.
 * TransactionService.executeTrade() 는 감사 로그의 존재를 전혀 모른다.
 *
 * @Order(HIGHEST_PRECEDENCE) 를 주는 이유:
 *   트랜잭션 AOP(@Transactional, 기본 LOWEST_PRECEDENCE)보다 바깥에서 돌아야
 *   after-returning 시점에 커밋이 끝난 상태의 자산 스냅샷을 읽을 수 있다.
 *
 * Actuator 메트릭(stock.trade.*)도 여기서 함께 올린다.
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TradeAuditAspect {

    private final TradeAuditService tradeAuditService;
    private final Counter successCounter;
    private final Counter failureCounter;

    public TradeAuditAspect(TradeAuditService tradeAuditService, MeterRegistry meterRegistry) {
        this.tradeAuditService = tradeAuditService;
        this.successCounter = Counter.builder("stock.trade.count")
                .description("매매 실행 건수")
                .tag("result", "success")
                .register(meterRegistry);
        this.failureCounter = Counter.builder("stock.trade.count")
                .description("매매 실행 건수")
                .tag("result", "failure")
                .register(meterRegistry);
    }

    @Pointcut("execution(* com.skala.stock.service.TransactionService.executeTrade(..)) && args(request)")
    public void executeTrade(TradeRequestDto request) {
    }

    @AfterReturning(pointcut = "executeTrade(request)", returning = "result", argNames = "request,result")
    public void afterTradeSuccess(TradeRequestDto request, TransactionDto result) {
        successCounter.increment();

        String message = String.format("[성공] %s %d주 @%d = %d원 (거래 ID: %d)",
                result.getType(), result.getQuantity(), result.getPrice(),
                result.getTotalAmount(), result.getId());

        tradeAuditService.record(request.getUserId(), request.getStockId(), request.getType(), message);
        log.info("거래 감사 로그 기록: userId={}, {}", request.getUserId(), message);
    }

    @AfterThrowing(pointcut = "executeTrade(request)", throwing = "e", argNames = "request,e")
    public void afterTradeFailure(TradeRequestDto request, Throwable e) {
        failureCounter.increment();

        String message = String.format("[실패] %s %s주 - %s",
                request.getType(), request.getQuantity(), e.getMessage());

        // 거래 트랜잭션은 롤백되지만, REQUIRES_NEW 로 열린 감사 로그는 남는다
        tradeAuditService.record(request.getUserId(), request.getStockId(), request.getType(), message);
        log.warn("거래 실패 감사 로그 기록: userId={}, {}", request.getUserId(), message);
    }
}
