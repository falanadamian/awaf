package com.falana.awaf.filter;

import com.falana.awaf.filter.configuration.IpFilterConfiguration;
import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class IpFilter implements WebFilter, Ordered {

    private final IpFilterConfiguration configuration;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();

        if (configuration.getRequestPredicate().test(request)) {
            response.getHeaders().set(configuration.getHeaderName(), configuration.getHeaderValueOnRejected());
            return configuration.getOnRejectResponse();
        }

        response.getHeaders().add(configuration.getHeaderName(), configuration.getHeaderValueOnAccepted());
        return webFilterChain.filter(serverWebExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
