package com.skala.teammatch.service;

import com.skala.teammatch.dto.PortfolioFileDto;
import com.skala.teammatch.entity.Member;
import com.skala.teammatch.entity.PortfolioFile;
import com.skala.teammatch.exception.BusinessException;
import com.skala.teammatch.repository.PortfolioFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioFileService {

    private final PortfolioFileRepository portfolioFileRepository;
    private final MemberService memberService;
    private final StorageService storageService;

    @Transactional
    public PortfolioFileDto upload(Long memberId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("업로드할 파일이 없습니다");
        }
        Member member = memberService.findMemberOrThrow(memberId);

        String storedPath = storageService.store(file, memberId);

        PortfolioFile portfolioFile = PortfolioFile.builder()
                .member(member)
                .originalFilename(file.getOriginalFilename())
                .storedPath(storedPath)
                .build();

        return toDto(portfolioFileRepository.save(portfolioFile));
    }

    public List<PortfolioFileDto> getFiles(Long memberId) {
        return portfolioFileRepository.findByMemberId(memberId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PortfolioFileDto toDto(PortfolioFile portfolioFile) {
        return PortfolioFileDto.builder()
                .id(portfolioFile.getId())
                .memberId(portfolioFile.getMember().getId())
                .originalFilename(portfolioFile.getOriginalFilename())
                .storedPath(portfolioFile.getStoredPath())
                .uploadedAt(portfolioFile.getUploadedAt())
                .build();
    }
}
