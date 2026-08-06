package com.skala.stock.repository;

import com.skala.stock.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByUserIdAndStockIdOrderByTransactionDateDesc(Long userId, Long stockId);

    /** 종목 삭제 전, 거래 이력이 있는지 확인한다 */
    boolean existsByStockId(Long stockId);

    /** 사용자 삭제 시 거래 이력을 함께 정리한다 */
    void deleteByUserId(Long userId);
}
