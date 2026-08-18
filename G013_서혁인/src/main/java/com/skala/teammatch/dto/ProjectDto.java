package com.skala.teammatch.dto;

import com.skala.teammatch.entity.Project.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {
    private Long id;
    private Long leaderId;
    private String leaderNickname;
    private String title;
    private String description;
    private ProjectStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private List<RecruitSlotDto> slots;
    private List<String> techStacks;
    private int memberCount;
}
