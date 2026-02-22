package com.zenon.tradeflow.service;


import com.zenon.tradeflow.exception.AssetNotFoundException;
import com.zenon.tradeflow.exception.TradeException;
import com.zenon.tradeflow.model.Asset;
import com.zenon.tradeflow.model.CryptoAsset;
import com.zenon.tradeflow.model.FiatAsset;
import com.zenon.tradeflow.model.StockAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {

    private final RestClient binanceClient;

    public Asset getCryptoPrice(String symbol) {
        // RestClient ile modern veri çekme
        var response = binanceClient.get()
                .uri("/ticker/price?symbol=" + symbol)
                .retrieve()
                // 4xx ve 5xx hatalarını burada yakalıyoruz
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new AssetNotFoundException(symbol);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new TradeException("Binance sunucularına ulaşılamıyor");
                })
                .body(Map.class);

        double price = Double.parseDouble(response.get("price").toString());

        return new CryptoAsset(symbol, price, "Binance Smart Chain");
    }

    public String analyzeAsset(Asset asset) {
        // Java 21 Pattern Matching for Switch
        return switch (asset) {
            case CryptoAsset c -> "🚀 Kripto Varlık: " + c.symbol() + " - Ağ: " + c.network() + " - Fiyat: $" + c.price();
            case StockAsset s  -> "📈 Hisse Senedi: " + s.symbol() + " - Borsa: " + s.exchange();
            case FiatAsset f   -> "💵 Döviz: " + f.symbol() + " - Ülke: " + f.country();
        };
    }
}