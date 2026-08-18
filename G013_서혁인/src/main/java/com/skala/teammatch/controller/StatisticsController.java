package com.skala.teammatch.controller;

import com.skala.teammatch.dto.TechStackDemandDto;
import com.skala.teammatch.dto.UserRankingDto;
import com.skala.teammatch.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "통계", description = "MyBatis 기반 통계/랭킹 조회 API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/tech-stacks/top10")
    @Operation(summary = "가장 수요가 많은 기술 스택 TOP 10")
    public ResponseEntity<List<TechStackDemandDto>> getTopDemandedTechStacks() {
        return ResponseEntity.ok(statisticsService.getTopDemandedTechStacks());
    }

    @GetMapping("/users/top-completed")
    @Operation(summary = "최다 프로젝트 완료 유저 랭킹 TOP 10")
    public ResponseEntity<List<UserRankingDto>> getTopCompletedUsers() {
        return ResponseEntity.ok(statisticsService.getTopCompletedUsers());
    }
}
