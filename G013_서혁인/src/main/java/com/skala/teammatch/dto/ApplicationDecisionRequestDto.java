package com.skala.teammatch.dto;

import com.skala.teammatch.entity.ApplicationDecisionLog.DecisionType;
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
public class ApplicationDecisionRequestDto {

    @NotNull(message = "처리자(팀장) ID는 필수입니다")
    private Long actorMemberId;

    @NotNull(message = "수락/거절 여부는 필수입니다")
    private DecisionType decision;
}
