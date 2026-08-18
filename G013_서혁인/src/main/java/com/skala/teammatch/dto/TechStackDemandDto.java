package com.skala.teammatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** MyBatis 통계: 가장 수요가 많은 기술 스택 TOP 10 (project_tech_stacks 태그 집계) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechStackDemandDto {
    private String techName;
    private Long demandCount;
}
