package com.falana.awaf.context;

import org.springframework.http.server.reactive.ServerHttpRequest;

@FunctionalInterface
public interface CacheKeyRetriever {
    String retrieve(ServerHttpRequest request);
}
