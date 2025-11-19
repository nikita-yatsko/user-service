package com.userservise.app.service.Impl;

import com.userservise.app.model.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient.Builder builder,
                             @Value("${auth.service.url}") String baseUrl) {
        this.webClient = builder.baseUrl("http://auth-service:8081").build();
    }

    public AuthResponse validate(String token) {
        return webClient.post()
                .uri("/api/auth/validate")
                .bodyValue(Map.of("token", token))
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .block();
    }
}
