package com.skala.teammatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 합격/불합격 처리 행위의 성공·실패 이력. AOP(DecisionAuditAspect)가 채워 넣으며
 * TeamApplicationService 는 이 테이블의 존재를 전혀 알지 못한다.
 */
@Entity
@Table(name = "application_decision_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisionType decision;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 500)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum DecisionType {
        ACCEPT, REJECT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
