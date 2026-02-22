package com.zenon.tradeflow.model;

public record ErrorAsset(String symbol, String message) implements Asset {
    @Override
    public double price() {
        return 0.0; // Hatalı varlığın fiyatı 0'dır
    }
}