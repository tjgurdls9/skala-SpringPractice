package com.skala.teammatch.controller;

import com.skala.teammatch.dto.MemberDto;
import com.skala.teammatch.dto.MemberProfileUpdateRequestDto;
import com.skala.teammatch.dto.MemberSignupRequestDto;
import com.skala.teammatch.dto.PortfolioFileDto;
import com.skala.teammatch.service.MemberService;
import com.skala.teammatch.service.PortfolioFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원 관리", description = "회원가입/프로필/포트폴리오 파일 업로드 API")
public class MemberController {

    private final MemberService memberService;
    private final PortfolioFileService portfolioFileService;

    @PostMapping
    @Operation(summary = "회원가입")
    public ResponseEntity<MemberDto> signup(@Valid @RequestBody MemberSignupRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.signup(request));
    }

    @GetMapping
    @Operation(summary = "회원 목록 조회")
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 단건 조회")
    public ResponseEntity<MemberDto> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMember(id));
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "자기소개/포트폴리오 링크 수정",
            description = "지원 시 필요한 최소 이력서 정보(자기소개 20자 이상, 포트폴리오 링크)를 채운다")
    public ResponseEntity<MemberDto> updateProfile(@PathVariable Long id,
                                                    @RequestBody MemberProfileUpdateRequestDto request) {
        return ResponseEntity.ok(memberService.updateProfile(id, request));
    }

    @PostMapping(value = "/{id}/portfolio-file", consumes = "multipart/form-data")
    @Operation(summary = "포트폴리오 파일 업로드",
            description = "local 프로필에서는 로컬 디스크에, prod 프로필에서는 S3(시뮬레이션)에 저장된다")
    public ResponseEntity<PortfolioFileDto> uploadPortfolioFile(@PathVariable Long id,
                                                                 @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioFileService.upload(id, file));
    }

    @GetMapping("/{id}/portfolio-file")
    @Operation(summary = "업로드한 포트폴리오 파일 목록 조회")
    public ResponseEntity<List<PortfolioFileDto>> getPortfolioFiles(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioFileService.getFiles(id));
    }
}
