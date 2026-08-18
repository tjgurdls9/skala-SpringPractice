package com.skala.teammatch.dto;

import com.skala.teammatch.entity.RecruitRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateRequestDto {

    @NotNull(message = "지원자 ID는 필수입니다")
    private Long applicantId;

    @NotNull(message = "지원할 역할은 필수입니다")
    private RecruitRole role;

    private String message;
}
