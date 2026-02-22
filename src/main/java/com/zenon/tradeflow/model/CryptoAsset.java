package com.zenon.tradeflow.model;

public record CryptoAsset(String symbol, double price, String network) implements Asset {}
