package com.skala.teammatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Profile 별로 local/prod yml 에서 값을 override 하는 파일 저장소 설정. */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        String type,
        Local local,
        S3 s3
) {
    public record Local(String directory) {
    }

    public record S3(String bucket, String region) {
    }
}
