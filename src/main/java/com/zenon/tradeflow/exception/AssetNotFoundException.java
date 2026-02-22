package com.zenon.tradeflow.exception;

public class AssetNotFoundException extends TradeException {
    public AssetNotFoundException(String symbol) {
        super("Varlık bulunamadı: " + symbol);
    }
}