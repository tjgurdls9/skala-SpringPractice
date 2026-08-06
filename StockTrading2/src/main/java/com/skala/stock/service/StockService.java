package com.skala.stock.service;

import com.skala.stock.dto.StockDto;
import com.skala.stock.entity.Stock;
import com.skala.stock.exception.BusinessException;
import com.skala.stock.exception.ResourceNotFoundException;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public StockDto createStock(StockDto stockDto) {
        if (stockRepository.existsByCode(stockDto.getCode())) {
            throw new BusinessException("이미 존재하는 종목 코드입니다: " + stockDto.getCode());
        }

        Stock stock = Stock.builder()
                .code(stockDto.getCode())
                .name(stockDto.getName())
                .currentPrice(stockDto.getCurrentPrice())
                .previousPrice(stockDto.getPreviousPrice())
                .build();

        Stock savedStock = stockRepository.save(stock);
        return convertToDto(savedStock);
    }

    public StockDto getStockById(Long id) {
        return convertToDto(findStockOrThrow(id));
    }

    /** 종목 코드로 조회 */
    public StockDto getStockByCode(String code) {
        Stock stock = stockRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("주식을 찾을 수 없습니다. 종목 코드: " + code));
        return convertToDto(stock);
    }

    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 주식 수정.
     *
     * 더티 체킹으로 반영되므로 save() 를 명시적으로 호출하지 않아도 되지만,
     * 의도를 드러내기 위해 저장을 호출한다.
     * 현재가를 바꿀 때는 직전 현재가를 previousPrice 로 자동 이월한다.
     */
    @Transactional
    public StockDto updateStock(Long id, StockDto stockDto) {
        Stock stock = findStockOrThrow(id);

        // 코드를 바꾸는 경우 다른 종목과 중복되면 안 된다
        if (!stock.getCode().equals(stockDto.getCode()) && stockRepository.existsByCode(stockDto.getCode())) {
            throw new BusinessException("이미 존재하는 종목 코드입니다: " + stockDto.getCode());
        }

        if (stockDto.getPreviousPrice() != null) {
            stock.setPreviousPrice(stockDto.getPreviousPrice());
        } else if (!stock.getCurrentPrice().equals(stockDto.getCurrentPrice())) {
            stock.setPreviousPrice(stock.getCurrentPrice());
        }

        stock.setCode(stockDto.getCode());
        stock.setName(stockDto.getName());
        stock.setCurrentPrice(stockDto.getCurrentPrice());

        return convertToDto(stockRepository.save(stock));
    }

    /**
     * 주식 삭제.
     *
     * 보유 중이거나 거래 이력이 있으면 참조 무결성이 깨지므로 삭제를 막는다.
     */
    @Transactional
    public void deleteStock(Long id) {
        Stock stock = findStockOrThrow(id);

        if (portfolioRepository.existsByStockId(id)) {
            throw new BusinessException("보유 중인 사용자가 있어 삭제할 수 없습니다: " + stock.getCode());
        }
        if (transactionRepository.existsByStockId(id)) {
            throw new BusinessException("거래 이력이 있어 삭제할 수 없습니다: " + stock.getCode());
        }

        stockRepository.delete(stock);
    }

    private Stock findStockOrThrow(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("주식을 찾을 수 없습니다: " + id));
    }

    private StockDto convertToDto(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .code(stock.getCode())
                .name(stock.getName())
                .currentPrice(stock.getCurrentPrice())
                .previousPrice(stock.getPreviousPrice())
                .build();
    }
}
