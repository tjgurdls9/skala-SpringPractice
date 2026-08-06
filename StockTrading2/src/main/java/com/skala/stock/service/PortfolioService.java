package com.skala.stock.service;

import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.entity.Portfolio;
import com.skala.stock.entity.Stock;
import com.skala.stock.entity.User;
import com.skala.stock.exception.ResourceNotFoundException;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public List<PortfolioDto> getUserPortfolio(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        return portfolios.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /** 특정 주식의 보유 현황 조회 */
    public PortfolioDto getPortfolio(Long userId, Long stockId) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "보유 중인 종목이 아닙니다. userId=" + userId + ", stockId=" + stockId));
        return convertToDto(portfolio);
    }

    /** 매도 가능 수량 검증용. 보유하고 있지 않으면 null 을 돌려준다. */
    public PortfolioDto findPortfolioOrNull(Long userId, Long stockId) {
        return portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .map(this::convertToDto)
                .orElse(null);
    }

    /** 매수 시 호출. 이미 보유 중이면 평균 매수가를 재계산한다. */
    @Transactional
    public PortfolioDto addToPortfolio(Long userId, Long stockId, Long quantity, Long price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("주식을 찾을 수 없습니다: " + stockId));

        Portfolio existing = portfolioRepository.findByUserIdAndStockId(userId, stockId).orElse(null);

        if (existing != null) {
            Long totalQuantity = existing.getQuantity() + quantity;
            Long totalCost = (existing.getAveragePrice() * existing.getQuantity()) + (price * quantity);

            existing.setQuantity(totalQuantity);
            existing.setAveragePrice(totalCost / totalQuantity);
            return convertToDto(portfolioRepository.save(existing));
        }

        Portfolio newPortfolio = Portfolio.builder()
                .user(user)
                .stock(stock)
                .quantity(quantity)
                .averagePrice(price)
                .build();
        return convertToDto(portfolioRepository.save(newPortfolio));
    }

    /** 매도 후 남은 수량을 반영한다. 0 이하가 되면 보유 목록에서 제거한다. */
    @Transactional
    public PortfolioDto updatePortfolio(Long userId, Long stockId, Long quantity) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "보유 중인 종목이 아닙니다. userId=" + userId + ", stockId=" + stockId));

        if (quantity <= 0) {
            portfolioRepository.delete(portfolio);
            return null;
        }

        portfolio.setQuantity(quantity);
        return convertToDto(portfolioRepository.save(portfolio));
    }

    /** 전량 매도 시 보유 목록에서 제거한다. */
    @Transactional
    public void removeFromPortfolio(Long userId, Long stockId) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "보유 중인 종목이 아닙니다. userId=" + userId + ", stockId=" + stockId));
        portfolioRepository.delete(portfolio);
    }

    private PortfolioDto convertToDto(Portfolio portfolio) {
        Stock stock = portfolio.getStock();
        Long currentPrice = stock.getCurrentPrice();
        Long totalValue = portfolio.getQuantity() * currentPrice;
        Long profitLoss = totalValue - (portfolio.getQuantity() * portfolio.getAveragePrice());

        return PortfolioDto.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUser().getId())
                .username(portfolio.getUser().getUsername())
                .stockId(stock.getId())
                .stockCode(stock.getCode())
                .stockName(stock.getName())
                .quantity(portfolio.getQuantity())
                .averagePrice(portfolio.getAveragePrice())
                .currentPrice(currentPrice)
                .totalValue(totalValue)
                .profitLoss(profitLoss)
                .build();
    }
}
