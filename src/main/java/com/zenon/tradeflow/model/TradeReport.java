package com.zenon.tradeflow.model;

import java.time.Instant;
import java.util.List;

public record TradeReport(
        Instant timestamp,
        List<Asset> assets,
        double totalValue,
        String threadInfo // Hangi thread tipinin çalıştığını görmek için
) {}
