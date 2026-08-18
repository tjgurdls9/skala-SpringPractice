package com.skala.teammatch.dto;

import com.skala.teammatch.entity.RecruitRole;
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
public class RecruitSlotDto {
    private Long id;
    private RecruitRole role;
    private Integer capacity;
    private Integer filledCount;
}
