package com.skala.teammatch.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("[404] {} - {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        log.warn("[400] {} - {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("[400] {} - 검증 실패 {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "요청 값 검증에 실패했습니다", request, fieldErrors);
    }

    /**
     * 요청 본문(JSON)을 읽지 못한 경우 — 잘못된 JSON 문법, 없는 Enum 값 등.
     * 서버 잘못이 아니라 보낸 값이 잘못된 것이므로 500이 아닌 400으로 응답한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("[400] {} - 요청 본문을 읽을 수 없음: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "요청 본문(JSON) 형식이 올바르지 않습니다", request, null);
    }

    /** URL 경로/파라미터의 타입이 맞지 않는 경우 (예: /api/projects/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("'%s' 값의 형식이 올바르지 않습니다: %s", e.getName(), e.getValue());
        log.warn("[400] {} - {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    /** 매핑된 API도 정적 리소스도 없는 경로 요청 — 500이 아닌 404로 응답한다. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("[404] {} - 존재하지 않는 경로", request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "요청하신 경로를 찾을 수 없습니다", request, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("[400] {} - 업로드 용량 초과", request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, "업로드 파일 용량이 너무 큽니다", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("[500] {} - {}", request.getRequestURI(), e.getMessage(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다", request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                String message,
                                                HttpServletRequest request,
                                                Map<String, String> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
