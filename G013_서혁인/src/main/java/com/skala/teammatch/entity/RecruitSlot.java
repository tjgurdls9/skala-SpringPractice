package com.skala.teammatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 프로젝트의 역할별 모집 정원. (project, role) 조합은 유일하다. */
@Entity
@Table(name = "recruit_slots", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "role"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecruitRole role;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "filled_count", nullable = false)
    @Builder.Default
    private Integer filledCount = 0;

    public boolean isFull() {
        return filledCount >= capacity;
    }
}
