package com.skala.teammatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    /** 자기소개 최소 길이. 이보다 짧으면 이력서 미작성으로 간주한다. */
    private static final int MIN_INTRODUCTION_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 1000)
    private String introduction; // 자기소개

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl; // 포트폴리오/이력서 링크

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** 지원에 필요한 최소한의 이력서(자기소개+포트폴리오)가 작성되어 있는지 여부 */
    public boolean isResumeComplete() {
        return introduction != null && introduction.length() >= MIN_INTRODUCTION_LENGTH
                && portfolioUrl != null && !portfolioUrl.isBlank();
    }
}
