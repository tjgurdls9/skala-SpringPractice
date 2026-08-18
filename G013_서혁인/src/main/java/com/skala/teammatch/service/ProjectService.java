package com.skala.teammatch.service;

import com.skala.teammatch.dto.ProjectCreateRequestDto;
import com.skala.teammatch.dto.ProjectDto;
import com.skala.teammatch.dto.RecruitSlotDto;
import com.skala.teammatch.dto.RecruitSlotRequestDto;
import com.skala.teammatch.entity.Member;
import com.skala.teammatch.entity.Project;
import com.skala.teammatch.entity.Project.ProjectStatus;
import com.skala.teammatch.entity.ProjectMember;
import com.skala.teammatch.entity.ProjectTechStack;
import com.skala.teammatch.entity.RecruitSlot;
import com.skala.teammatch.exception.BusinessException;
import com.skala.teammatch.exception.ResourceNotFoundException;
import com.skala.teammatch.repository.ProjectMemberRepository;
import com.skala.teammatch.repository.ProjectRepository;
import com.skala.teammatch.repository.ProjectTechStackRepository;
import com.skala.teammatch.repository.RecruitSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final RecruitSlotRepository recruitSlotRepository;
    private final ProjectTechStackRepository projectTechStackRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberService memberService;
    private final ChatRoomService chatRoomService;

    @Transactional
    public ProjectDto createProject(ProjectCreateRequestDto request) {
        Member leader = memberService.findMemberOrThrow(request.getLeaderId());

        Project project = Project.builder()
                .leader(leader)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ProjectStatus.RECRUITING)
                .deadline(request.getDeadline())
                .build();
        projectRepository.save(project);

        for (RecruitSlotRequestDto slotRequest : request.getSlots()) {
            recruitSlotRepository.save(RecruitSlot.builder()
                    .project(project)
                    .role(slotRequest.role())
                    .capacity(slotRequest.capacity())
                    .filledCount(0)
                    .build());
        }

        if (request.getTechStacks() != null) {
            for (String techName : request.getTechStacks()) {
                if (techName == null || techName.isBlank()) {
                    continue;
                }
                projectTechStackRepository.save(ProjectTechStack.builder()
                        .project(project)
                        .techName(techName.trim())
                        .build());
            }
        }

        // 리더는 모집 정원과 별개로 프로젝트 생성과 동시에 팀원으로 합류한다
        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .member(leader)
                .role(request.getLeaderRole())
                .build());

        // 지원자가 한 명도 수락되기 전이라도 리더가 자기 프로젝트 채팅방에 바로 접속할 수 있어야 한다
        chatRoomService.createRoomForProject(project);

        return toDto(project);
    }

    public List<ProjectDto> getProjects(ProjectStatus status) {
        List<Project> projects = status == null
                ? projectRepository.findAllByOrderByCreatedAtDesc()
                : projectRepository.findByStatusOrderByCreatedAtDesc(status);
        return projects.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProjectDto getProject(Long projectId) {
        return toDto(findProjectOrThrow(projectId));
    }

    @Transactional
    public ProjectDto changeStatus(Long projectId, Long actorMemberId, ProjectStatus newStatus) {
        Project project = findProjectOrThrow(projectId);
        if (!project.getLeader().getId().equals(actorMemberId)) {
            throw new BusinessException("프로젝트 리더만 상태를 변경할 수 있습니다");
        }
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new BusinessException("이미 완료된 프로젝트입니다");
        }
        project.setStatus(newStatus);
        return toDto(project);
    }

    Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));
    }

    private ProjectDto toDto(Project project) {
        List<RecruitSlotDto> slots = recruitSlotRepository.findByProjectId(project.getId()).stream()
                .map(slot -> RecruitSlotDto.builder()
                        .id(slot.getId())
                        .role(slot.getRole())
                        .capacity(slot.getCapacity())
                        .filledCount(slot.getFilledCount())
                        .build())
                .collect(Collectors.toList());

        List<String> techStacks = projectTechStackRepository.findByProjectId(project.getId()).stream()
                .map(ProjectTechStack::getTechName)
                .collect(Collectors.toList());

        int memberCount = projectMemberRepository.findByProjectId(project.getId()).size();

        return ProjectDto.builder()
                .id(project.getId())
                .leaderId(project.getLeader().getId())
                .leaderNickname(project.getLeader().getNickname())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .deadline(project.getDeadline())
                .createdAt(project.getCreatedAt())
                .slots(slots)
                .techStacks(techStacks)
                .memberCount(memberCount)
                .build();
    }
}
