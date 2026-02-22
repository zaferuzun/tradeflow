package com.zenon.tradeflow.model;

public sealed interface Asset permits CryptoAsset, StockAsset, FiatAsset {
    String symbol();
    double price();
}

