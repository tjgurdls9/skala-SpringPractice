package com.skala.teammatch.controller;

import com.skala.teammatch.dto.ApplicationCreateRequestDto;
import com.skala.teammatch.dto.ApplicationDecisionRequestDto;
import com.skala.teammatch.dto.ApplicationDto;
import com.skala.teammatch.service.TeamApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "지원 관리", description = "프로젝트 지원 및 팀장의 수락/거절 처리 API")
public class ApplicationController {

    private final TeamApplicationService teamApplicationService;

    @PostMapping("/api/projects/{projectId}/applications")
    @Operation(summary = "프로젝트 지원", description = "모집 마감 프로젝트, 이력서 미작성 회원, 정원 초과 역할에는 예외가 발생한다")
    public ResponseEntity<ApplicationDto> apply(@PathVariable Long projectId,
                                                 @Valid @RequestBody ApplicationCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamApplicationService.apply(projectId, request));
    }

    @GetMapping("/api/projects/{projectId}/applications")
    @Operation(summary = "프로젝트별 지원자 목록 조회")
    public ResponseEntity<List<ApplicationDto>> getApplicationsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(teamApplicationService.getApplicationsByProject(projectId));
    }

    @GetMapping("/api/members/{memberId}/applications")
    @Operation(summary = "내 지원 현황 조회")
    public ResponseEntity<List<ApplicationDto>> getApplicationsByApplicant(@PathVariable Long memberId) {
        return ResponseEntity.ok(teamApplicationService.getApplicationsByApplicant(memberId));
    }

    @PostMapping("/api/applications/{applicationId}/decision")
    @Operation(summary = "지원 수락/거절 (핵심 트랜잭션)",
            description = "수락 시 정원 증가, 지원자 상태를 '팀 합류'로 변경, 팀 채팅방 개설/합류까지 한 트랜잭션으로 처리된다. "
                    + "성공/실패 여부는 AOP 로 감사 로그에 남는다")
    public ResponseEntity<ApplicationDto> decide(@PathVariable Long applicationId,
                                                  @Valid @RequestBody ApplicationDecisionRequestDto request) {
        ApplicationDto result = teamApplicationService.decide(
                applicationId, request.getActorMemberId(), request.getDecision());
        return ResponseEntity.ok(result);
    }
}
