package com.skala.stock.mapper;

import com.skala.stock.dto.DailyTransactionSummaryDto;
import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.dto.TradeSnapshotDto;
import com.skala.stock.dto.TransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 분석/집계 전용 SQL Mapper.
 *
 * CRUD 는 JPA(Spring Data JPA)로, 조인·집계가 들어가는 분석 조회는 MyBatis 로 처리한다.
 * 실제 SQL 은 resources/mapper/StockMapper.xml 에 있다.
 */
@Mapper
public interface StockMapper {

    /** 1. 포트폴리오 평가 손익 조회 */
    List<PortfolioDto> findPortfolioWithProfitLoss(@Param("userId") Long userId);

    /** 1-1. 특정 주식 한 종목의 평가 손익 조회 */
    PortfolioDto findPortfolioItemWithProfitLoss(@Param("userId") Long userId,
                                                 @Param("stockId") Long stockId);

    /** 2. 거래 내역 상세 조회 (사용자/종목 정보까지 조인) */
    List<TransactionDto> findTransactionsWithDetails(@Param("userId") Long userId);

    /** 2-1. 거래 단건 상세 조회 */
    TransactionDto findTransactionDetailById(@Param("id") Long id);

    /** 3. 특정 주식 거래 내역 조회 */
    List<TransactionDto> findStockTransactionsWithDetails(@Param("userId") Long userId,
                                                         @Param("stockId") Long stockId);

    /** 4. 총 자산 조회 (현금 + 주식 평가 금액) */
    TradeSnapshotDto findAssetSummary(@Param("userId") Long userId);

    /** 5. 총 수익률(%) 조회 */
    Double findTotalReturnRate(@Param("userId") Long userId);

    /** 6. 종목별 거래 통계 조회 */
    TransactionStatisticsDto findTransactionStatistics(@Param("stockId") Long stockId);

    /** 6-1. 사용자 기준 전 종목 거래 통계 */
    List<TransactionStatisticsDto> findUserTransactionStatistics(@Param("userId") Long userId);

    /** 7. 일별 거래 내역 조회 (특정 일자) */
    List<TransactionDto> findDailyTransactions(@Param("userId") Long userId,
                                               @Param("date") LocalDate date);

    /** 7-1. 일자별 거래 집계 */
    List<DailyTransactionSummaryDto> findDailyTransactionSummary(@Param("userId") Long userId);
}
