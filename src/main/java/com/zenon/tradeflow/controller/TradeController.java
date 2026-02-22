package com.zenon.tradeflow.controller;

import com.zenon.tradeflow.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class TradeController {

    private final MarketService marketService;

    @GetMapping("/price/{symbol}")
    public String getPrice(@PathVariable String symbol) {
        var asset = marketService.getCryptoPrice(symbol);
        return marketService.analyzeAsset(asset);
    }
}