package com.example.gateway.Configuration;

import com.example.gateway.dto.UserDto;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    private final WebClient.Builder webClientBuilder;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                throw new RuntimeException("no key authorization");
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String[] parts = authHeader.split(" ");
            if (parts.length !=2 || !"Bearer".equals(parts[0])){
                throw new RuntimeException("authorization no contains token");
            }

            return webClientBuilder.build()
                    .get()
                    .uri("http://User/User/validate-token/" + parts[1])
                    .retrieve().bodyToMono(UserDto.class)
                    .map(response -> {
                        exchange.getRequest()
                                .mutate()
                                .header("X-auth-user", String.valueOf(response));
                        return exchange;
                    }).flatMap(chain::filter);
        };
    }

    public static class Config{
    }
}
