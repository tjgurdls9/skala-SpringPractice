package com.skala.stock.service;

import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.dto.TradeRequestDto;
import com.skala.stock.dto.TransactionDto;
import com.skala.stock.entity.Stock;
import com.skala.stock.entity.Transaction;
import com.skala.stock.entity.User;
import com.skala.stock.exception.BusinessException;
import com.skala.stock.exception.ResourceNotFoundException;
import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.TransactionRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioService portfolioService;

    /**
     * 주식 매매 실행.
     *
     * 잔액 차감/증가, 포트폴리오 갱신, 거래 이력 저장이 모두 성공하거나 모두 실패해야 하므로
     * 하나의 쓰기 트랜잭션으로 묶는다. 중간에 예외가 나면 전부 롤백된다.
     */
    @Transactional
    public TransactionDto executeTrade(TradeRequestDto tradeRequest) {
        if (tradeRequest.getQuantity() <= 0) {
            throw new BusinessException("거래 수량은 1주 이상이어야 합니다: " + tradeRequest.getQuantity());
        }

        User user = userRepository.findById(tradeRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + tradeRequest.getUserId()));
        Stock stock = stockRepository.findById(tradeRequest.getStockId())
                .orElseThrow(() -> new ResourceNotFoundException("주식을 찾을 수 없습니다: " + tradeRequest.getStockId()));

        Long currentPrice = stock.getCurrentPrice();
        Long totalAmount = currentPrice * tradeRequest.getQuantity();

        if (tradeRequest.getType() == Transaction.TransactionType.BUY) {
            buy(user, stock, tradeRequest.getQuantity(), currentPrice, totalAmount);
        } else {
            sell(user, stock, tradeRequest.getQuantity(), totalAmount);
        }

        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .user(user)
                .stock(stock)
                .type(tradeRequest.getType())
                .quantity(tradeRequest.getQuantity())
                .price(currentPrice)
                .totalAmount(totalAmount)
                .build();

        return convertToDto(transactionRepository.save(transaction));
    }

    private void buy(User user, Stock stock, Long quantity, Long price, Long totalAmount) {
        if (user.getBalance() < totalAmount) {
            throw new BusinessException(
                    "잔액이 부족합니다. 필요 금액: " + totalAmount + ", 보유 금액: " + user.getBalance());
        }
        user.setBalance(user.getBalance() - totalAmount);
        portfolioService.addToPortfolio(user.getId(), stock.getId(), quantity, price);
    }

    private void sell(User user, Stock stock, Long quantity, Long totalAmount) {
        PortfolioDto portfolio = portfolioService.findPortfolioOrNull(user.getId(), stock.getId());
        long held = portfolio == null ? 0L : portfolio.getQuantity();
        if (held < quantity) {
            throw new BusinessException("보유 수량이 부족합니다. 보유 수량: " + held + ", 매도 수량: " + quantity);
        }

        user.setBalance(user.getBalance() + totalAmount);

        long remaining = held - quantity;
        if (remaining > 0) {
            portfolioService.updatePortfolio(user.getId(), stock.getId(), remaining);
        } else {
            portfolioService.removeFromPortfolio(user.getId(), stock.getId());
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<TransactionDto> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /** 거래 상세 조회 (읽기 전용) */
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("거래를 찾을 수 없습니다: " + id));
        return convertToDto(transaction);
    }

    /** 특정 주식 거래 내역 조회 (JPA 버전) */
    @Transactional(readOnly = true)
    public List<TransactionDto> getUserStockTransactions(Long userId, Long stockId) {
        return transactionRepository.findByUserIdAndStockIdOrderByTransactionDateDesc(userId, stockId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TransactionDto convertToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .username(transaction.getUser().getUsername())
                .stockId(transaction.getStock().getId())
                .stockCode(transaction.getStock().getCode())
                .stockName(transaction.getStock().getName())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
