package com.skala.stock.controller;

import com.skala.stock.dto.StockDto;
import com.skala.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "주식 관리", description = "주식 CRUD API")
public class StockController {

    private final StockService stockService;

    @PostMapping
    @Operation(summary = "주식 생성", description = "새로운 주식을 등록합니다")
    public ResponseEntity<StockDto> createStock(@Valid @RequestBody StockDto stockDto) {
        StockDto createdStock = stockService.createStock(stockDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStock);
    }

    @GetMapping("/{id}")
    @Operation(summary = "주식 조회 (ID)", description = "ID로 주식을 조회합니다")
    public ResponseEntity<StockDto> getStockById(@PathVariable Long id) {
        StockDto stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "주식 조회 (종목 코드)", description = "종목 코드로 주식을 조회합니다 (예: 005930)")
    public ResponseEntity<StockDto> getStockByCode(@PathVariable String code) {
        return ResponseEntity.ok(stockService.getStockByCode(code));
    }

    @GetMapping
    @Operation(summary = "전체 주식 조회", description = "모든 주식을 조회합니다")
    public ResponseEntity<List<StockDto>> getAllStocks() {
        List<StockDto> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "주식 수정",
            description = "종목 정보를 수정합니다. previousPrice 를 생략하면 기존 현재가가 전일 종가로 이월됩니다")
    public ResponseEntity<StockDto> updateStock(@PathVariable Long id,
                                                @Valid @RequestBody StockDto stockDto) {
        return ResponseEntity.ok(stockService.updateStock(id, stockDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "주식 삭제",
            description = "종목을 삭제합니다. 보유자나 거래 이력이 있으면 삭제할 수 없습니다")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}
