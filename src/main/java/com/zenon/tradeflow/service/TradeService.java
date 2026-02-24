package com.zenon.tradeflow.service;


import com.zenon.tradeflow.exception.AssetNotFoundException;
import com.zenon.tradeflow.exception.TradeException;
import com.zenon.tradeflow.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {

    private final RestClient binanceClient;

    private final ApplicationContext context; // Spring context'ini alıyoruz


    @Cacheable(value = "market-prices", key = "#symbol", sync = true)
    public Asset getCryptoPrice(String symbol) {
        log.info("🔥 Binance API çağrılıyor: {}", symbol);     // RestClient ile modern veri çekme
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
            case ErrorAsset e  -> "⚠️ HATA: [" + e.symbol() + "] verisi alınamadı! Sebep: " + e.message();
        };
    }

    public TradeReport getBulkPrices(List<String> symbols) {
        // Kendi Proxy'mizi context üzerinden çekiyoruz
        TradeService self = context.getBean(TradeService.class);
        // Sanal Thread Executor'ı oluşturuyoruz
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Her sembol için asenkron bir görev (task) başlatıyoruz
            List<CompletableFuture<Asset>> futures = symbols.stream()
                    .map(symbol -> CompletableFuture.supplyAsync(() -> self.getCryptoPrice(symbol), executor)
                            // Kritik Yer: Hata oluşursa bunu yakalayıp ErrorAsset'e çeviriyoruz
                            .handle((asset, ex) -> {
                                if (ex != null) {
                                    // ex.getCause() kullanıyoruz çünkü CompletableFuture hatayı sarmalar
                                    String errorMsg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                                    return new ErrorAsset(symbol, errorMsg);
                                }
                                return asset;
                            })
                    )
                    .toList();

            // Tüm görevlerin bitmesini bekleyip sonuçları topluyoruz
            List<Asset> assets = futures.stream()
                    .map(CompletableFuture::join) // Her bir sonucu al (Hata olursa Exception fırlatır)
                    .toList();

            double totalValue = assets.stream()
                    .mapToDouble(Asset::price)
                    .sum();

            return new TradeReport(
                    Instant.now(),
                    assets,
                    totalValue,
                    Thread.currentThread().toString()
            );
        }
    }

}