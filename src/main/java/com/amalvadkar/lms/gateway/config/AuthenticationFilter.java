package com.amalvadkar.lms.gateway.config;

import com.amalvadkar.lms.gateway.ApplicationProperties;
import com.amalvadkar.lms.gateway.client.request.VerifyTokenRequest;
import com.amalvadkar.lms.gateway.client.response.VerifyTokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLE_ID = "X-Role-Id";
    private static final String HEADER_DEVICE = "X-Device";

    private final ApplicationProperties appProps;
    private final WebClient webClient;
    private final RouterValidator routerValidator;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(ApplicationProperties appProps,WebClient.Builder webBuilder, RouterValidator routerValidator, ObjectMapper objectMapper) {
        super(Config.class);
        this.appProps = appProps;
        this.webClient = webBuilder.build();
        this.routerValidator = routerValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("pre filter :: {}", request.getLocalAddress());
            if (isSecuredEndpoint(request)) {

                if (authIsMissingIn(request)) {
                    VerifyTokenResponse verifyTokenResponse = new VerifyTokenResponse(UNAUTHORIZED.value(), "Auth header is missing");
                    return onError(exchange, verifyTokenResponse);
                }

                final String fullToken = extractTokenFrom(request);
                String[] tokenArray = fullToken.split(" ");

                VerifyTokenRequest verifyTokenRequest = prepareVerifyTokenRequest(tokenArray);

                return webClient.post()
                        .uri(appProps.verifyTokenUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(verifyTokenRequest)
                        .retrieve()
                        .onStatus(
                                HttpStatus.INTERNAL_SERVER_ERROR::equals,
                                clientResponse -> Mono.error(new Exception("Something went wrong, please try later.."))
                        )
                        .bodyToMono(VerifyTokenResponse.class)
                        .flatMap(verifyTokenResponse -> {
                            log.info("verify token response = {}", verifyTokenResponse);
                            if (verifyTokenResponse.status() != 200) {
                                return onError(exchange, verifyTokenResponse);
                            }
                            return populateRequestWithHeaderAndPreservedBody(exchange, chain, verifyTokenResponse);
                        });
            }
            return chain.filter(exchange);
        };
    }


    private static VerifyTokenRequest prepareVerifyTokenRequest(String[] tokenArray) {
        if (tokenArray.length == 2) {
            return new VerifyTokenRequest(tokenArray[1]);
        } else {
            return new VerifyTokenRequest(tokenArray[0]);
        }
    }

    private static String extractTokenFrom(ServerHttpRequest request) {
        return requireNonNull(request.getHeaders().get(AUTHORIZATION)).getFirst();
    }

    private boolean isSecuredEndpoint(ServerHttpRequest request) {
        return routerValidator.isSecured.test(request);
    }

    private static boolean authIsMissingIn(ServerHttpRequest request) {
        return !request.getHeaders().containsKey(AUTHORIZATION);
    }

    private static Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(UNAUTHORIZED);
        return response.setComplete();
    }

    private Mono<Void> onError(ServerWebExchange exchange, VerifyTokenResponse responseObj) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.valueOf(responseObj.status()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = toJson(responseObj);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String toJson(VerifyTokenResponse responseObj) {
        try {
            return objectMapper.writeValueAsString(responseObj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Config {
    }

    private Mono<Void> populateRequestWithHeaderAndPreservedBody(ServerWebExchange exchange,
                                                                 GatewayFilterChain chain,
                                                                 VerifyTokenResponse verifyTokenResponse) {
        log.info("Verified userId={}, roleId={}", verifyTokenResponse.userId(), verifyTokenResponse.roleId());
        log.info("Setting header values");
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    Flux<DataBuffer> cachedFlux = Flux.defer(() ->
                            Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header(HEADER_USER_ID, verifyTokenResponse.userId())
                            .header(HEADER_ROLE_ID, verifyTokenResponse.roleId())
                            .header(HEADER_DEVICE, verifyTokenResponse.device())
                            .build();

                    ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(mutatedRequest) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedFlux;
                        }
                    };

                    return chain.filter(exchange.mutate().request(decoratedRequest).build());
                });
    }
}
