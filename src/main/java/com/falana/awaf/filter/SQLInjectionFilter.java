package com.falana.awaf.filter;

import com.falana.awaf.detector.SQLInjectionDetector;
import com.falana.awaf.exception.SQLInjectionException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class SQLInjectionFilter implements WebFilter, Ordered {

    private final SQLInjectionDetector sqlInjectionDetector;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();

        if (StringUtils.isEmpty(request.getURI().getQuery())) {
            return webFilterChain.filter(serverWebExchange);
        }

        List<String> queryParametersValues = List.of(
                request.getURI().getQuery().split("&"))
                .stream()
                .map(queryParameter -> queryParameter.split("=", 2)[1])
                .collect(Collectors.toList());

        for (String queryParameter : queryParametersValues) {
            if (sqlInjectionDetector.detect(queryParameter)) {
                response.getHeaders().set("SQLInjection-Protection", "request-rejected");
                return Mono.error(new SQLInjectionException(sqlInjectionDetector.getProperties().getRejectionMessage()));
            }
        }

        response.getHeaders().add("SQLInjection-Protection", "request-accepted");
        return webFilterChain.filter(serverWebExchange);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
