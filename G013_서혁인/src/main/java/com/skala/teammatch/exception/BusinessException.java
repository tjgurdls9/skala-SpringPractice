package com.skala.teammatch.exception;

/**
 * 마감된 프로젝트 지원, 이력서 미작성, 정원 초과 등 업무 규칙을 위반했을 때 사용한다. (HTTP 400)
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
