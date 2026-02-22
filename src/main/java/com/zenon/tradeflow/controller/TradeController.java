package com.zenon.tradeflow.controller;

import com.zenon.tradeflow.model.TradeReport;
import com.zenon.tradeflow.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping("/price/{symbol}")
    public String getPrice(@PathVariable String symbol) {
        var asset = tradeService.getCryptoPrice(symbol);
        return tradeService.analyzeAsset(asset);
    }

    @GetMapping("/bulk")
    public TradeReport getBulkPrices(@RequestParam List<String> symbols) {
        // Örn: /api/market/bulk?symbols=BTCUSDT,ETHUSDT,BNBUSDT,SOLUSDT
        return tradeService.getBulkPrices(symbols);
    }
}