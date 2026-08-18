package com.skala.teammatch.controller;

import com.skala.teammatch.dto.ProjectCreateRequestDto;
import com.skala.teammatch.dto.ProjectDto;
import com.skala.teammatch.entity.Project.ProjectStatus;
import com.skala.teammatch.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "프로젝트 관리", description = "팀원 모집 프로젝트 생성/조회/상태 변경 API")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "프로젝트(모집글) 생성",
            description = "역할별 모집 정원(slots)과 기술 스택 태그를 함께 등록하고, 리더를 자동으로 팀원에 합류시킨다")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회", description = "status 파라미터로 RECRUITING/CLOSED/COMPLETED 필터링 가능")
    public ResponseEntity<List<ProjectDto>> getProjects(@RequestParam(required = false) ProjectStatus status) {
        return ResponseEntity.ok(projectService.getProjects(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "프로젝트 상세 조회", description = "모집 정원 현황, 기술 스택, 현재 팀원 수를 함께 반환한다")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "프로젝트 상태 변경",
            description = "리더만 변경 가능. 모집 마감(CLOSED) 또는 완료(COMPLETED) 처리 시 사용한다")
    public ResponseEntity<ProjectDto> changeStatus(@PathVariable Long id,
                                                    @RequestParam Long actorMemberId,
                                                    @RequestParam ProjectStatus status) {
        return ResponseEntity.ok(projectService.changeStatus(id, actorMemberId, status));
    }
}
