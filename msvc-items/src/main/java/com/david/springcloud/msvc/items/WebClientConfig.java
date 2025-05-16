package com.david.springcloud.msvc.items;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${config.base-url.endpoint.msvc-products}")
    private String url;

    @Bean
    @LoadBalanced
    WebClient.Builder webClient() {
        return WebClient.builder().baseUrl(url);
    }

}
