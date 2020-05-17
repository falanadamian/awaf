package com.falana.awaf.context;

import com.falana.awaf.restriction.RateLimitAction;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.AbstractMap;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface RateLimitGuardian {
    AbstractMap.SimpleEntry<RateLimitAction, CompletableFuture<ConsumptionProbe>> examine(ServerHttpRequest serverHttpRequest);
}
