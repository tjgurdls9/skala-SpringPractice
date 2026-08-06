package com.skala.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * StockMapper.xml 을 작성하면서 확인한 SQL 제약을 고정해두는 테스트.
 *
 * 나중에 쿼리를 손볼 때 같은 실수를 반복하지 않도록 "왜 이렇게 썼는지"를 코드로 남긴다.
 */
@SpringBootTest
class SqlDialectCheckTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("날짜 함수: H2 는 DATE() 를 지원하지 않으므로 CAST(... AS DATE) 를 써야 한다")
    void dateFunctionIsNotSupported() {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE DATE(transaction_date) = CURRENT_DATE",
                Long.class))
                .isInstanceOf(BadSqlGrammarException.class);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE CAST(transaction_date AS DATE) = CURRENT_DATE",
                Long.class)).isNotNull();
    }

    @Test
    @DisplayName("수익률 계산: 명시적 DOUBLE 캐스팅으로 소수점까지 산출된다")
    void returnRateKeepsDecimals() {
        // user1: 평가금액 1,300,000 / 매수원금 1,290,000 → +0.78%
        Double rate = jdbcTemplate.queryForObject("""
                SELECT ROUND((CAST(SUM(p.quantity * s.current_price) - SUM(p.quantity * p.average_price) AS DOUBLE)
                       / CAST(SUM(p.quantity * p.average_price) AS DOUBLE)) * 100, 2)
                FROM portfolios p INNER JOIN stocks s ON p.stock_id = s.id
                WHERE p.user_id = 1
                """, Double.class);

        assertThat(rate).isEqualTo(0.78);
    }
}
