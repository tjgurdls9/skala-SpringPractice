package com.skala.teammatch.aop;

import com.skala.teammatch.dto.ApplicationDto;
import com.skala.teammatch.entity.ApplicationDecisionLog.DecisionType;
import com.skala.teammatch.service.DecisionAuditService;
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
 * 합격/불합격 처리 감사 로그 AOP.
 *
 * 팀장이 지원을 수락/거절할 때마다 성공/실패 여부를 훔쳐서 통계용 로그로 남긴다.
 * TeamApplicationService.decide() 는 이 로그의 존재를 전혀 모른다 (핵심 로직과 관심사 분리).
 *
 * Order 를 트랜잭션 AOP(@Transactional, 기본 LOWEST_PRECEDENCE)보다 낮은 값으로 주는 이유:
 *   그래야 이 Aspect 가 트랜잭션 어드바이스보다 바깥에서 돌아서, after-returning 시점에는
 *   이미 커밋이 끝난 상태를 보게 되고, after-throwing 시점에는 본 트랜잭션이 롤백된 뒤에도
 *   감사 로그(REQUIRES_NEW)는 남길 수 있다.
 *   단, Integer.MIN_VALUE(HIGHEST_PRECEDENCE) 를 그대로 쓰면 Spring 이 args() 바인딩을 위해
 *   내부적으로 등록하는 ExposeInvocationInterceptor(동일하게 HIGHEST_PRECEDENCE) 보다 이 Aspect가
 *   먼저 실행되어 "No MethodInvocation found" 오류가 나므로, 한 단계 낮은 값을 사용한다.
 *
 * Actuator 메트릭(teammatch.decision.count)도 여기서 함께 집계한다.
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DecisionAuditAspect {

    private final DecisionAuditService decisionAuditService;
    private final Counter successCounter;
    private final Counter failureCounter;

    public DecisionAuditAspect(DecisionAuditService decisionAuditService, MeterRegistry meterRegistry) {
        this.decisionAuditService = decisionAuditService;
        this.successCounter = Counter.builder("teammatch.decision.count")
                .description("지원 수락/거절 처리 건수")
                .tag("result", "success")
                .register(meterRegistry);
        this.failureCounter = Counter.builder("teammatch.decision.count")
                .description("지원 수락/거절 처리 건수")
                .tag("result", "failure")
                .register(meterRegistry);
    }

    @Pointcut("execution(* com.skala.teammatch.service.TeamApplicationService.decide(..)) "
            + "&& args(applicationId, actorMemberId, decision)")
    public void decide(Long applicationId, Long actorMemberId, DecisionType decision) {
    }

    @AfterReturning(pointcut = "decide(applicationId, actorMemberId, decision)", returning = "result",
            argNames = "applicationId,actorMemberId,decision,result")
    public void afterDecisionSuccess(Long applicationId, Long actorMemberId, DecisionType decision, ApplicationDto result) {
        successCounter.increment();

        String message = "[성공] %s 처리 - 지원자: %s, 프로젝트: %s, 역할: %s".formatted(
                decision, result.getApplicantNickname(), result.getProjectTitle(), result.getRole());

        decisionAuditService.record(applicationId, decision, true, message);
        log.info("합격/불합격 처리 감사 로그 기록: applicationId={}, {}", applicationId, message);
    }

    @AfterThrowing(pointcut = "decide(applicationId, actorMemberId, decision)", throwing = "e",
            argNames = "applicationId,actorMemberId,decision,e")
    public void afterDecisionFailure(Long applicationId, Long actorMemberId, DecisionType decision, Throwable e) {
        failureCounter.increment();

        String message = "[실패] %s 처리 시도 - actorMemberId: %d, 사유: %s".formatted(
                decision, actorMemberId, e.getMessage());

        // 본 트랜잭션은 롤백되지만, REQUIRES_NEW 로 열린 감사 로그는 남는다
        decisionAuditService.record(applicationId, decision, false, message);
        log.warn("합격/불합격 처리 실패 감사 로그 기록: applicationId={}, {}", applicationId, message);
    }
}
