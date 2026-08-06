package com.skala.stock.actuator;

import com.skala.stock.entity.TradeAuditLog;
import com.skala.stock.service.TradeAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 커스텀 Actuator 엔드포인트: GET /actuator/tradeaudit
 *
 * AOP 가 남긴 거래 감사 로그를 운영자가 바로 확인할 수 있게 노출한다.
 * 업무 API(/api/**)가 아니라 운영용 엔드포인트라서 actuator 아래에 둔다.
 */
@Component
@Endpoint(id = "tradeaudit")
@RequiredArgsConstructor
public class TradeAuditEndpoint {

    private static final int MAX_ITEMS = 20;

    private final TradeAuditService tradeAuditService;

    @ReadOperation
    public Map<String, Object> auditLogs() {
        List<TradeAuditLog> logs = tradeAuditService.findAll();

        List<Map<String, Object>> recent = logs.stream()
                .sorted(Comparator.comparing(TradeAuditLog::getId).reversed())
                .limit(MAX_ITEMS)
                .map(this::toMap)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", logs.size());
        result.put("recent", recent);
        return result;
    }

    private Map<String, Object> toMap(TradeAuditLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUserId());
        map.put("stockId", log.getStockId());
        map.put("type", log.getType());
        map.put("message", log.getMessage());
        map.put("totalAssets", log.getTotalAssets());
        map.put("totalReturnRate", log.getTotalReturnRate());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }
}
