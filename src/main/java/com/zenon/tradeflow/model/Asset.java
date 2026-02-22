package com.zenon.tradeflow.model;

public sealed interface Asset permits CryptoAsset, ErrorAsset, FiatAsset, StockAsset {
    String symbol();
    double price();
}

