package com.skala.stock.exception;

/**
 * 조회 대상이 존재하지 않을 때 사용한다. (HTTP 404)
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
