package com.zenon.tradeflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Bean
    public RestClient binanceClient() {
        return RestClient.builder()
                .baseUrl("https://api.binance.com/api/v3")
                .build();
    }
}