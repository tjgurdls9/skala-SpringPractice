package com.skala.teammatch.service;

import com.skala.teammatch.dto.ApplicationCreateRequestDto;
import com.skala.teammatch.dto.ApplicationDto;
import com.skala.teammatch.entity.ApplicationDecisionLog.DecisionType;
import com.skala.teammatch.entity.Member;
import com.skala.teammatch.entity.Project;
import com.skala.teammatch.entity.Project.ProjectStatus;
import com.skala.teammatch.entity.ProjectMember;
import com.skala.teammatch.entity.RecruitSlot;
import com.skala.teammatch.entity.TeamApplication;
import com.skala.teammatch.entity.TeamApplication.ApplicationStatus;
import com.skala.teammatch.exception.BusinessException;
import com.skala.teammatch.exception.ResourceNotFoundException;
import com.skala.teammatch.repository.ProjectMemberRepository;
import com.skala.teammatch.repository.RecruitSlotRepository;
import com.skala.teammatch.repository.TeamApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamApplicationService {

    private final TeamApplicationRepository teamApplicationRepository;
    private final RecruitSlotRepository recruitSlotRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final MemberService memberService;
    private final ChatRoomService chatRoomService;

    @Transactional
    public ApplicationDto apply(Long projectId, ApplicationCreateRequestDto request) {
        Project project = projectService.findProjectOrThrow(projectId);
        if (project.getStatus() != ProjectStatus.RECRUITING) {
            throw new BusinessException("모집이 마감된 프로젝트에는 지원할 수 없습니다: " + project.getTitle());
        }

        Member applicant = memberService.findMemberOrThrow(request.getApplicantId());
        if (!applicant.isResumeComplete()) {
            throw new BusinessException("자기소개와 포트폴리오 링크를 먼저 작성해야 지원할 수 있습니다");
        }

        RecruitSlot slot = recruitSlotRepository.findByProjectIdAndRole(projectId, request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "해당 프로젝트에는 %s 역할 모집이 없습니다".formatted(request.getRole())));
        if (slot.isFull()) {
            throw new BusinessException("%s 역할은 이미 정원이 모두 찼습니다".formatted(slot.getRole()));
        }

        if (teamApplicationRepository.existsByProjectIdAndApplicantIdAndSlotId(
                projectId, applicant.getId(), slot.getId())) {
            throw new BusinessException("이미 해당 역할에 지원했습니다");
        }

        TeamApplication application = TeamApplication.builder()
                .project(project)
                .slot(slot)
                .applicant(applicant)
                .status(ApplicationStatus.PENDING)
                .message(request.getMessage())
                .build();

        return toDto(teamApplicationRepository.save(application));
    }

    public List<ApplicationDto> getApplicationsByProject(Long projectId) {
        return teamApplicationRepository.findByProjectIdOrderByAppliedAtDesc(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ApplicationDto> getApplicationsByApplicant(Long applicantId) {
        return teamApplicationRepository.findByApplicantIdOrderByAppliedAtDesc(applicantId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 핵심 트랜잭션: 팀장의 지원 수락/거절 처리.
     *
     * 수락 시 (1) 슬롯 정원 +1, (2) 지원자 상태를 '팀 합류'(ACCEPTED)로 변경, (3) ProjectMember 생성,
     * (4) 모든 슬롯이 찼으면 프로젝트 자동 마감, (5) 팀 채팅방 개설/합류 까지 전부 한 트랜잭션으로 묶는다.
     * 도중에 예외가 나면 전부 롤백된다.
     *
     * 이 메서드의 성공/실패는 DecisionAuditAspect 가 가로채 별도 이력 테이블에 기록한다.
     */
    @Transactional
    public ApplicationDto decide(Long applicationId, Long actorMemberId, DecisionType decision) {
        TeamApplication application = teamApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("지원 내역을 찾을 수 없습니다: " + applicationId));

        Project project = application.getProject();
        if (!project.getLeader().getId().equals(actorMemberId)) {
            throw new BusinessException("프로젝트 리더만 지원을 수락/거절할 수 있습니다");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("이미 처리된 지원입니다: " + application.getStatus());
        }

        application.setDecidedAt(LocalDateTime.now());

        if (decision == DecisionType.REJECT) {
            application.setStatus(ApplicationStatus.REJECTED);
            return toDto(application);
        }

        RecruitSlot slot = application.getSlot();
        if (slot.isFull()) {
            throw new BusinessException("%s 역할은 이미 정원이 모두 찼습니다".formatted(slot.getRole()));
        }

        slot.setFilledCount(slot.getFilledCount() + 1);
        application.setStatus(ApplicationStatus.ACCEPTED);

        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .member(application.getApplicant())
                .role(slot.getRole())
                .build());

        boolean allSlotsFull = recruitSlotRepository.findByProjectId(project.getId()).stream()
                .allMatch(RecruitSlot::isFull);
        if (allSlotsFull) {
            project.setStatus(ProjectStatus.CLOSED);
        }

        chatRoomService.ensureRoomAndAddMember(project, application.getApplicant());

        return toDto(application);
    }

    private ApplicationDto toDto(TeamApplication application) {
        return ApplicationDto.builder()
                .id(application.getId())
                .projectId(application.getProject().getId())
                .projectTitle(application.getProject().getTitle())
                .applicantId(application.getApplicant().getId())
                .applicantNickname(application.getApplicant().getNickname())
                .role(application.getSlot().getRole())
                .status(application.getStatus())
                .message(application.getMessage())
                .appliedAt(application.getAppliedAt())
                .decidedAt(application.getDecidedAt())
                .build();
    }
}
