package com.zenon.tradeflow.model;

public record StockAsset(String symbol, double price, String exchange) implements Asset {}
