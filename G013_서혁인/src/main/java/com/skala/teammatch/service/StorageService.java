package com.skala.teammatch.service;

import org.springframework.web.multipart.MultipartFile;

/** Profile 에 따라 로컬 디스크(local) 또는 S3(prod)로 구현체가 바뀌는 파일 저장소. */
public interface StorageService {

    /** 파일을 저장하고, 저장된 위치를 나타내는 경로/URL을 반환한다. */
    String store(MultipartFile file, Long memberId);
}
