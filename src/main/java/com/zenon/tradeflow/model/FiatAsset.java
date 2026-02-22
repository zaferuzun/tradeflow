package com.zenon.tradeflow.model;

public record FiatAsset(String symbol, double price, String country) implements Asset {}
