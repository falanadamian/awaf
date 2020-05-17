package com.falana.awaf.context;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.function.Predicate;

@FunctionalInterface
public interface RequestMatcher extends Predicate<ServerHttpRequest> {

    RequestMatcher EMPTY = serverHttpRequest -> false;

    boolean test(ServerHttpRequest serverHttpRequest);
}
