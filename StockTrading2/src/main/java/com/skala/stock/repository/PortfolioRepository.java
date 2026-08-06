package com.skala.stock.repository;

import com.skala.stock.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);
    Optional<Portfolio> findByUserIdAndStockId(Long userId, Long stockId);
    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    /** 종목 삭제 전, 보유 중인 사용자가 있는지 확인한다 */
    boolean existsByStockId(Long stockId);

    /** 사용자 삭제 시 보유 종목을 함께 정리한다 */
    void deleteByUserId(Long userId);
}
