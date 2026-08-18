package com.skala.teammatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private Long id;
    private String email;
    private String nickname;
    private String introduction;
    private String portfolioUrl;
    private boolean resumeComplete;
    private LocalDateTime createdAt;
}
