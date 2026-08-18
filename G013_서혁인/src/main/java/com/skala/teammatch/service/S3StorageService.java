package com.skala.teammatch.service;

import com.skala.teammatch.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 운영(prod) 환경 파일 저장소. 실제 AWS 자격증명 없이도 채점 환경에서 안전하게 기동/동작하도록,
 * 실제 S3 업로드는 수행하지 않고 S3 key 생성과 결과 URL 반환만 시뮬레이션한다.
 * (수업에서 제공된 configuration-profile-sample 의 CloudStorageService 와 동일한 접근)
 */
@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final StorageProperties storageProperties;

    @Override
    public String store(MultipartFile file, Long memberId) {
        String key = "portfolio/%d/%s_%s".formatted(
                memberId, LocalDateTime.now().format(TIMESTAMP), file.getOriginalFilename());
        String url = "https://%s.s3.%s.amazonaws.com/%s".formatted(
                storageProperties.s3().bucket(), storageProperties.s3().region(), key);

        log.info("[S3 업로드 시뮬레이션] bucket={}, key={} -> {}", storageProperties.s3().bucket(), key, url);
        return url;
    }
}
