package com.skala.stock.controller;

import com.skala.stock.dto.DailyTransactionSummaryDto;
import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.dto.TradeSnapshotDto;
import com.skala.stock.dto.TransactionDto;
import com.skala.stock.mapper.TransactionStatisticsDto;
import com.skala.stock.service.StockAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 분석/고급 조회 API. 내부 구현은 모두 MyBatis(SQL Mapper) 기반이다.
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "분석", description = "포트폴리오/거래 분석 API (MyBatis 기반)")
public class StockAnalysisController {

    private final StockAnalysisService stockAnalysisService;

    @GetMapping("/portfolio/{userId}")
    @Operation(summary = "1. 포트폴리오 평가 손익 조회",
            description = "보유 종목별 평가 금액과 평가 손익을 조회합니다")
    public ResponseEntity<List<PortfolioDto>> getPortfolioProfitLoss(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getPortfolioWithProfitLoss(userId));
    }

    @GetMapping("/portfolio/{userId}/stock/{stockId}")
    @Operation(summary = "1-1. 특정 주식 평가 손익 조회",
            description = "보유 종목 중 한 종목의 평가 손익을 조회합니다")
    public ResponseEntity<PortfolioDto> getPortfolioItemProfitLoss(@PathVariable Long userId,
                                                                   @PathVariable Long stockId) {
        return ResponseEntity.ok(stockAnalysisService.getPortfolioItemWithProfitLoss(userId, stockId));
    }

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "2. 거래 내역 상세 조회",
            description = "사용자·종목 정보를 조인한 거래 내역을 조회합니다")
    public ResponseEntity<List<TransactionDto>> getTransactionDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getTransactionsWithDetails(userId));
    }

    @GetMapping("/transaction/{id}")
    @Operation(summary = "2-1. 거래 단건 상세 조회", description = "거래 ID로 상세 내역 1건을 조회합니다")
    public ResponseEntity<TransactionDto> getTransactionDetail(@PathVariable Long id) {
        return ResponseEntity.ok(stockAnalysisService.getTransactionDetail(id));
    }

    @GetMapping("/transactions/{userId}/stock/{stockId}")
    @Operation(summary = "3. 특정 주식 거래 내역 조회",
            description = "특정 사용자의 특정 종목 거래 내역을 조회합니다")
    public ResponseEntity<List<TransactionDto>> getStockTransactionDetails(@PathVariable Long userId,
                                                                           @PathVariable Long stockId) {
        return ResponseEntity.ok(stockAnalysisService.getStockTransactionsWithDetails(userId, stockId));
    }

    @GetMapping("/assets/{userId}")
    @Operation(summary = "4. 총 자산 조회",
            description = "보유 현금 + 주식 평가 금액을 조회합니다")
    public ResponseEntity<Map<String, Object>> getTotalAssets(@PathVariable Long userId) {
        TradeSnapshotDto summary = stockAnalysisService.getAssetSummary(userId);
        return ResponseEntity.ok(Map.of(
                "userId", summary.getUserId(),
                "username", summary.getUsername(),
                "cashBalance", summary.getCashBalance(),
                "stockValue", summary.getStockValue(),
                "totalAssets", summary.getTotalAssets()
        ));
    }

    @GetMapping("/return-rate/{userId}")
    @Operation(summary = "5. 총 수익률 조회",
            description = "매수 원금 대비 평가 금액 기준 총 수익률(%)을 조회합니다")
    public ResponseEntity<Map<String, Object>> getTotalReturnRate(@PathVariable Long userId) {
        TradeSnapshotDto summary = stockAnalysisService.getAssetSummary(userId);
        return ResponseEntity.ok(Map.of(
                "userId", summary.getUserId(),
                "investedAmount", summary.getInvestedAmount(),
                "stockValue", summary.getStockValue(),
                "evaluationProfitLoss", summary.getEvaluationProfitLoss(),
                "totalReturnRate", summary.getTotalReturnRate()
        ));
    }

    @GetMapping("/summary/{userId}")
    @Operation(summary = "4+5. 자산 종합 요약",
            description = "총 자산과 총 수익률을 한 번에 조회합니다")
    public ResponseEntity<TradeSnapshotDto> getAssetSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getAssetSummary(userId));
    }

    @GetMapping("/statistics/stock/{stockId}")
    @Operation(summary = "6. 거래 통계 조회",
            description = "특정 종목의 매수/매도 수량·금액 및 순매수 통계를 조회합니다")
    public ResponseEntity<TransactionStatisticsDto> getTransactionStatistics(@PathVariable Long stockId) {
        return ResponseEntity.ok(stockAnalysisService.getTransactionStatistics(stockId));
    }

    @GetMapping("/statistics/user/{userId}")
    @Operation(summary = "6-1. 사용자 거래 통계 조회",
            description = "특정 사용자의 종목별 매매 통계를 조회합니다")
    public ResponseEntity<List<TransactionStatisticsDto>> getUserTransactionStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getUserTransactionStatistics(userId));
    }

    @GetMapping("/transactions/{userId}/daily")
    @Operation(summary = "7. 일별 거래 내역 조회",
            description = "특정 일자(yyyy-MM-dd)의 거래 내역을 조회합니다. date 를 생략하면 오늘 기준입니다")
    public ResponseEntity<List<TransactionDto>> getDailyTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        return ResponseEntity.ok(stockAnalysisService.getDailyTransactions(userId, target));
    }

    @GetMapping("/transactions/{userId}/daily-summary")
    @Operation(summary = "7-1. 일자별 거래 집계 조회",
            description = "일자별 매수/매도 건수와 금액을 집계해 조회합니다")
    public ResponseEntity<List<DailyTransactionSummaryDto>> getDailyTransactionSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getDailyTransactionSummary(userId));
    }
}
