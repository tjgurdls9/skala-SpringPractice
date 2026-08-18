package com.skala.teammatch.dto;

import com.skala.teammatch.entity.RecruitRole;
import com.skala.teammatch.entity.TeamApplication.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDto {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long applicantId;
    private String applicantNickname;
    private RecruitRole role;
    private ApplicationStatus status;
    private String message;
    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;
}
