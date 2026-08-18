package com.skala.teammatch.service;

import com.skala.teammatch.dto.MemberDto;
import com.skala.teammatch.dto.MemberProfileUpdateRequestDto;
import com.skala.teammatch.dto.MemberSignupRequestDto;
import com.skala.teammatch.entity.Member;
import com.skala.teammatch.exception.BusinessException;
import com.skala.teammatch.exception.ResourceNotFoundException;
import com.skala.teammatch.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDto signup(MemberSignupRequestDto request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("이미 가입된 이메일입니다: " + request.getEmail());
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .nickname(request.getNickname())
                .introduction(request.getIntroduction())
                .portfolioUrl(request.getPortfolioUrl())
                .build();

        return toDto(memberRepository.save(member));
    }

    public MemberDto getMember(Long memberId) {
        return toDto(findMemberOrThrow(memberId));
    }

    public List<MemberDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberDto updateProfile(Long memberId, MemberProfileUpdateRequestDto request) {
        Member member = findMemberOrThrow(memberId);
        member.setIntroduction(request.getIntroduction());
        member.setPortfolioUrl(request.getPortfolioUrl());
        return toDto(member);
    }

    Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + memberId));
    }

    private MemberDto toDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .introduction(member.getIntroduction())
                .portfolioUrl(member.getPortfolioUrl())
                .resumeComplete(member.isResumeComplete())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
