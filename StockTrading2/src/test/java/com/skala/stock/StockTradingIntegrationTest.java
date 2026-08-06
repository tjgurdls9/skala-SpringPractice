package com.skala.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.stock.dto.TradeRequestDto;
import com.skala.stock.entity.Transaction;
import com.skala.stock.service.TradeAuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 주요 시나리오 통합 테스트.
 *
 * data.sql 로 들어간 초기 데이터(사용자 3명 / 종목 12개 / 거래 4건)를 전제로 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StockTradingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TradeAuditService tradeAuditService;

    @Test
    @DisplayName("초기 데이터: 사용자 3명, 종목 12건이 적재된다")
    void initialData() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)));
    }

    @Test
    @DisplayName("종목 코드로 조회한다")
    void getStockByCode() throws Exception {
        mockMvc.perform(get("/api/stocks/code/005930"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("삼성전자"));

        mockMvc.perform(get("/api/stocks/code/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("매수하면 잔액이 줄고 포트폴리오에 반영된다")
    void buyStock() throws Exception {
        TradeRequestDto request = TradeRequestDto.builder()
                .userId(3L)
                .stockId(4L) // 카카오 50,000원
                .type(Transaction.TransactionType.BUY)
                .quantity(2L)
                .build();

        mockMvc.perform(post("/api/transactions/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(100000));

        mockMvc.perform(get("/api/portfolios/user/3/stock/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.averagePrice").value(50000));
    }

    @Test
    @DisplayName("보유 수량보다 많이 매도하면 400 이고 감사 로그가 남는다")
    void sellMoreThanHeld() throws Exception {
        long before = tradeAuditService.findAll().size();

        TradeRequestDto request = TradeRequestDto.builder()
                .userId(3L)
                .stockId(5L)
                .type(Transaction.TransactionType.SELL)
                .quantity(999L)
                .build();

        mockMvc.perform(post("/api/transactions/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // 거래 트랜잭션은 롤백되지만 REQUIRES_NEW 로 기록한 감사 로그는 남는다
        assertThat(tradeAuditService.findAll()).hasSize((int) before + 1);
    }

    @Test
    @DisplayName("필수값이 빠지면 필드별 검증 오류를 돌려준다")
    void validationError() throws Exception {
        mockMvc.perform(post("/api/transactions/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.type").exists())
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    @DisplayName("MyBatis 분석 쿼리: 총 자산 = 현금 + 주식 평가 금액")
    void assetSummary() throws Exception {
        mockMvc.perform(get("/api/analysis/summary/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashBalance").value(1734000))
                .andExpect(jsonPath("$.stockValue").value(1300000))
                .andExpect(jsonPath("$.totalAssets").value(3034000))
                .andExpect(jsonPath("$.totalReturnRate").value(0.78));
    }

    @Test
    @DisplayName("MyBatis 분석 쿼리: 종목별 거래 통계")
    void transactionStatistics() throws Exception {
        mockMvc.perform(get("/api/analysis/statistics/stock/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBuyQuantity").value(8))
                .andExpect(jsonPath("$.totalSellQuantity").value(3))
                .andExpect(jsonPath("$.netQuantity").value(5));
    }

    @Test
    @DisplayName("Actuator 커스텀 health/endpoint 가 노출된다")
    void actuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.stockMarket.status").value("UP"))
                .andExpect(jsonPath("$.components.stockMarket.details.stockCount").value(12));

        mockMvc.perform(get("/actuator/tradeaudit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").exists());
    }
}
