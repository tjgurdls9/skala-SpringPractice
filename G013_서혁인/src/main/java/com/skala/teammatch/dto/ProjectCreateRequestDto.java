package com.skala.teammatch.dto;

import com.skala.teammatch.entity.RecruitRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateRequestDto {

    @NotNull(message = "리더 ID는 필수입니다")
    private Long leaderId;

    @NotNull(message = "리더가 맡을 역할은 필수입니다")
    private RecruitRole leaderRole;

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String description;
    private LocalDate deadline;

    @NotEmpty(message = "모집 역할(정원)을 한 개 이상 입력해야 합니다")
    private List<RecruitSlotRequestDto> slots;

    private List<String> techStacks;
}
