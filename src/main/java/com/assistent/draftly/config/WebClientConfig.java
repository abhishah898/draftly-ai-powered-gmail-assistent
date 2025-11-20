package com.assistent.draftly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(config -> config.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 20 MB
                .build();
    }
}
