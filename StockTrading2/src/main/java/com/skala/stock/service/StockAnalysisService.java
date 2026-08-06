package com.skala.stock.service;

import com.skala.stock.dto.DailyTransactionSummaryDto;
import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.dto.TradeSnapshotDto;
import com.skala.stock.dto.TransactionDto;
import com.skala.stock.exception.ResourceNotFoundException;
import com.skala.stock.mapper.StockMapper;
import com.skala.stock.mapper.TransactionStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 분석/집계 전용 서비스.
 *
 * 조인과 집계가 들어가는 조회라 JPA 대신 MyBatis(SQL Mapper)로 처리한다.
 * 전부 읽기 전용이므로 클래스 레벨에 readOnly 트랜잭션을 건다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockAnalysisService {

    private final StockMapper stockMapper;

    /** 1. 포트폴리오 평가 손익 조회 */
    public List<PortfolioDto> getPortfolioWithProfitLoss(Long userId) {
        return stockMapper.findPortfolioWithProfitLoss(userId);
    }

    /** 1-1. 특정 주식 평가 손익 조회 */
    public PortfolioDto getPortfolioItemWithProfitLoss(Long userId, Long stockId) {
        PortfolioDto portfolio = stockMapper.findPortfolioItemWithProfitLoss(userId, stockId);
        if (portfolio == null) {
            throw new ResourceNotFoundException(
                    "보유 중인 종목이 아닙니다. userId=" + userId + ", stockId=" + stockId);
        }
        return portfolio;
    }

    /** 2. 거래 내역 상세 조회 */
    public List<TransactionDto> getTransactionsWithDetails(Long userId) {
        return stockMapper.findTransactionsWithDetails(userId);
    }

    /** 2-1. 거래 단건 상세 조회 */
    public TransactionDto getTransactionDetail(Long id) {
        TransactionDto transaction = stockMapper.findTransactionDetailById(id);
        if (transaction == null) {
            throw new ResourceNotFoundException("거래를 찾을 수 없습니다: " + id);
        }
        return transaction;
    }

    /** 3. 특정 주식 거래 내역 조회 */
    public List<TransactionDto> getStockTransactionsWithDetails(Long userId, Long stockId) {
        return stockMapper.findStockTransactionsWithDetails(userId, stockId);
    }

    /** 4·5. 총 자산 + 총 수익률을 한 번에 담은 요약 */
    public TradeSnapshotDto getAssetSummary(Long userId) {
        TradeSnapshotDto summary = stockMapper.findAssetSummary(userId);
        if (summary == null) {
            throw new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }
        summary.setTotalReturnRate(getTotalReturnRate(userId));
        return summary;
    }

    /** 4. 총 자산 조회 */
    public Long getTotalAssets(Long userId) {
        return getAssetSummary(userId).getTotalAssets();
    }

    /** 5. 총 수익률(%) 조회 */
    public Double getTotalReturnRate(Long userId) {
        Double rate = stockMapper.findTotalReturnRate(userId);
        return rate == null ? 0.0 : rate;
    }

    /** 6. 종목별 거래 통계 조회 */
    public TransactionStatisticsDto getTransactionStatistics(Long stockId) {
        TransactionStatisticsDto statistics = stockMapper.findTransactionStatistics(stockId);
        if (statistics == null) {
            throw new ResourceNotFoundException("주식을 찾을 수 없습니다: " + stockId);
        }
        return statistics;
    }

    /** 6-1. 사용자 기준 전 종목 거래 통계 */
    public List<TransactionStatisticsDto> getUserTransactionStatistics(Long userId) {
        return stockMapper.findUserTransactionStatistics(userId);
    }

    /** 7. 일별 거래 내역 조회 (특정 일자) */
    public List<TransactionDto> getDailyTransactions(Long userId, LocalDate date) {
        return stockMapper.findDailyTransactions(userId, date);
    }

    /** 7-1. 일자별 거래 집계 */
    public List<DailyTransactionSummaryDto> getDailyTransactionSummary(Long userId) {
        return stockMapper.findDailyTransactionSummary(userId);
    }
}
