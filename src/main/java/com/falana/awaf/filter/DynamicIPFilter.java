package com.falana.awaf.filter;

import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.exception.BlacklistedException;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DynamicIPFilter implements WebFilter, Ordered {

    private final HazelcastInstance hazelcastInstance;

    private static String headerName = "DoS-Protection";
    private static String headerValue = "blacklisted";
    private static String message = "You have been blocked by DoS protection";

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();

        String clientIP = request.getRemoteAddress().getAddress().getHostAddress();

        if (hazelcastInstance.getList(AccessControlAction.DENY.name()).contains(clientIP)) {
            response.getHeaders().set(headerName, headerValue);
            return Mono.error(new BlacklistedException(message));
        }
        return webFilterChain.filter(serverWebExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
