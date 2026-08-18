package com.skala.teammatch.dto;

import com.skala.teammatch.entity.RecruitRole;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 프로젝트 생성 시 함께 전달되는 역할별 모집 정원. */
public record RecruitSlotRequestDto(
        @NotNull(message = "역할은 필수입니다") RecruitRole role,
        @NotNull(message = "정원은 필수입니다") @Min(value = 1, message = "정원은 1명 이상이어야 합니다") Integer capacity
) {
}
