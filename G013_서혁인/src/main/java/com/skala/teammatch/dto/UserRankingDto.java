package com.skala.teammatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** MyBatis 통계: 최다 프로젝트 완료 유저 랭킹 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRankingDto {
    private Long memberId;
    private String nickname;
    private Long completedCount;
}
