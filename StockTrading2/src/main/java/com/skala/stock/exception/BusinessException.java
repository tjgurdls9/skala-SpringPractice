package com.skala.stock.exception;

/**
 * 잔액 부족, 보유 수량 부족처럼 업무 규칙을 위반했을 때 사용한다. (HTTP 400)
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
