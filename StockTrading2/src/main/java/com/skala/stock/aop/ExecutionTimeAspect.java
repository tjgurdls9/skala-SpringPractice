package com.skala.stock.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 실행 시간 측정 AOP.
 *
 * 컨트롤러/서비스의 모든 메서드에 공통으로 걸리는 관심사(로깅·성능 측정)를
 * 비즈니스 코드에서 떼어내기 위한 Aspect다.
 * 느린 요청은 WARN 으로 올려서 Actuator 메트릭과 함께 병목을 찾을 때 쓴다.
 */
@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

    /** 200ms 를 넘으면 느린 호출로 본다 */
    private static final long SLOW_THRESHOLD_MS = 200L;

    @Pointcut("within(com.skala.stock.controller..*)")
    public void controllerLayer() {
    }

    @Pointcut("within(com.skala.stock.service..*)")
    public void serviceLayer() {
    }

    @Around("controllerLayer() || serviceLayer()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (elapsed >= SLOW_THRESHOLD_MS) {
                log.warn("[SLOW] {} - {}ms args={}", signature, elapsed, Arrays.toString(joinPoint.getArgs()));
            } else {
                log.debug("[TIME] {} - {}ms", signature, elapsed);
            }
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[FAIL] {} - {}ms ({}: {})", signature, elapsed, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
