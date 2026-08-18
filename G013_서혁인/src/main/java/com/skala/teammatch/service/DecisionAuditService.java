package com.skala.teammatch.service;

import com.skala.teammatch.entity.ApplicationDecisionLog;
import com.skala.teammatch.entity.ApplicationDecisionLog.DecisionType;
import com.skala.teammatch.repository.ApplicationDecisionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 합격/불합격 처리 감사 로그 저장 서비스.
 *
 * REQUIRES_NEW 로 별도 트랜잭션을 열기 때문에, 처리 로직이 예외로 롤백되더라도
 * "실패했다"는 감사 기록 자체는 남는다.
 */
@Service
@RequiredArgsConstructor
public class DecisionAuditService {

    private final ApplicationDecisionLogRepository applicationDecisionLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long applicationId, DecisionType decision, boolean success, String message) {
        applicationDecisionLogRepository.save(ApplicationDecisionLog.builder()
                .applicationId(applicationId)
                .decision(decision)
                .success(success)
                .message(truncate(message))
                .build());
    }

    @Transactional(readOnly = true)
    public List<ApplicationDecisionLog> findAll() {
        return applicationDecisionLogRepository.findAll();
    }

    private String truncate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
