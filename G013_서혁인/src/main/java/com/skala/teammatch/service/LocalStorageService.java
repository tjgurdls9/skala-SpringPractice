package com.skala.teammatch.service;

import com.skala.teammatch.config.StorageProperties;
import com.skala.teammatch.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 개발(local) 환경 파일 저장소. 설정된 로컬 디렉터리(기본 ./uploads/portfolio)에 실제로 파일을 저장한다.
 * app.storage.local.directory 로 경로를 바꿀 수 있다 (예: 로컬 PC의 바탕화면 경로).
 */
@Slf4j
@Service
@Profile("local | default")
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final StorageProperties storageProperties;

    @Override
    public String store(MultipartFile file, Long memberId) {
        try {
            Path directory = Path.of(storageProperties.local().directory());
            Files.createDirectories(directory);

            String storedFilename = "%d_%s_%s".formatted(
                    memberId, LocalDateTime.now().format(TIMESTAMP), file.getOriginalFilename());
            Path target = directory.resolve(storedFilename);
            file.transferTo(target);

            log.info("로컬 저장 완료: {}", target);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("파일 저장에 실패했습니다: " + e.getMessage());
        }
    }
}
