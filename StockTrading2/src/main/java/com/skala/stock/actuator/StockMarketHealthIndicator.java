package com.skala.stock.actuator;

import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 커스텀 Health Indicator.
 *
 * /actuator/health 안에 "stockMarket" 항목으로 노출된다.
 * DB 커넥션이 살아 있어도 종목 데이터가 비어 있으면 매매가 불가능하므로 DOWN 으로 본다.
 */
@Component("stockMarket")
@RequiredArgsConstructor
public class StockMarketHealthIndicator implements HealthIndicator {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    @Override
    public Health health() {
        try {
            long stockCount = stockRepository.count();
            long userCount = userRepository.count();

            Health.Builder builder = stockCount > 0 ? Health.up() : Health.down();
            return builder
                    .withDetail("stockCount", stockCount)
                    .withDetail("userCount", userCount)
                    .withDetail("reason", stockCount > 0 ? "거래 가능" : "등록된 종목이 없어 매매 불가")
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
