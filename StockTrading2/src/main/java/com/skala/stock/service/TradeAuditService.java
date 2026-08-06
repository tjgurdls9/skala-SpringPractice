package com.skala.stock.service;

import com.skala.stock.dto.TradeSnapshotDto;
import com.skala.stock.entity.TradeAuditLog;
import com.skala.stock.entity.Transaction;
import com.skala.stock.repository.TradeAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 거래 감사 로그 저장 서비스.
 *
 * REQUIRES_NEW 로 별도 트랜잭션을 열기 때문에, 호출한 쪽이 롤백되더라도 감사 기록은 남는다.
 * 거래 실패 사유를 남기는 게 이 로그의 핵심 목적이라 이렇게 분리했다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeAuditService {

    private final TradeAuditLogRepository tradeAuditLogRepository;
    private final StockAnalysisService stockAnalysisService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, Long stockId, Transaction.TransactionType type, String message) {
        TradeAuditLog.TradeAuditLogBuilder builder = TradeAuditLog.builder()
                .userId(userId)
                .stockId(stockId)
                .type(type)
                .message(truncate(message));

        try {
            TradeSnapshotDto snapshot = stockAnalysisService.getAssetSummary(userId);
            builder.totalAssets(snapshot.getTotalAssets())
                   .totalReturnRate(snapshot.getTotalReturnRate());
        } catch (RuntimeException e) {
            // 감사 로그 때문에 거래 흐름이 깨지면 안 되므로, 스냅샷 실패는 로그만 남기고 넘어간다
            log.warn("자산 스냅샷 계산 실패 (userId={}): {}", userId, e.getMessage());
        }

        tradeAuditLogRepository.save(builder.build());
    }

    @Transactional(readOnly = true)
    public List<TradeAuditLog> findAll() {
        return tradeAuditLogRepository.findAll();
    }

    private String truncate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
