package com.skala.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 일자별 거래 집계 결과다. MyBatis 집계 쿼리 전용 DTO라서 엔티티로 만들지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTransactionSummaryDto {

    private LocalDate transactionDate;
    private Long transactionCount;
    private Long buyCount;
    private Long sellCount;
    private Long buyAmount;
    private Long sellAmount;
    private Long netAmount; // 매수금액 - 매도금액 (순매수 금액)
}
