package com.example.gateway.Configuration;

import com.example.gateway.dto.UserDto;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    private final WebClient.Builder webClientBuilder;
    public AuthFilter(WebClient.Builder webClientBuilder){
        this.webClientBuilder=webClientBuilder;
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) ->{
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                throw new RuntimeException("Missing auth info");
            }
          String authHeader=  exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String[] parts=authHeader.split("");
            if(parts.length != 2 || !"Bearer".equals(parts[0])){
                throw new RuntimeException("incorrect auth structure");
            }
            return webClientBuilder.build()
                    .post()
                    .uri("http://service-users/users/validateToken?token"+parts[1])
                    .retrieve().bodyToMono(UserDto.class)
                    .map(userDto -> {
                        exchange.getRequest()
                                .mutate()
                                .header("x-auth_user_id",String.valueOf(userDto.getId()));
                            return exchange;
                    }).flatMap(chain::filter);
        } ;
    }
    public static class Config{
    }
}
