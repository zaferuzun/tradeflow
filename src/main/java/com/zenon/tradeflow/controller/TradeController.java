package com.zenon.tradeflow.controller;

import com.zenon.tradeflow.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}