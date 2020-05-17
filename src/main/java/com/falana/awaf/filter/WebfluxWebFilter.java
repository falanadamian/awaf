package com.falana.awaf.filter;

import com.falana.awaf.context.controls.BanType;
import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.context.configuration.FilterConfiguration;
import com.falana.awaf.exception.RateLimitException;
import com.falana.awaf.filter.configuration.IpFilterConfiguration;
import com.falana.awaf.restriction.RateLimitAction;
import com.hazelcast.core.HazelcastInstance;
import io.github.bucket4j.ConsumptionProbe;
import lombok.Builder;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

@Builder
public class WebfluxWebFilter implements WebFilter, Ordered {

    private final HazelcastInstance hazelcastInstance;

    private final FilterConfiguration filterConfig;
    private final Predicate<ServerHttpRequest> shouldFilterRequest;
    private final IpFilterConfiguration ipFilterConfiguration;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();
        List<CompletableFuture<Long>> rateLimitFutures = new ArrayList<>();
        List<CompletableFuture<Long>> refillTimeFutures = new ArrayList<>();

        AtomicBoolean allToSkip = new AtomicBoolean(true);
        AtomicBoolean anyToSkip = new AtomicBoolean(false);

        if (Objects.nonNull(ipFilterConfiguration)) {
            if (ipFilterConfiguration.getRequestPredicate().test(request)) {
                response.getHeaders().set(ipFilterConfiguration.getHeaderName(), ipFilterConfiguration.getHeaderValueOnRejected());
                return ipFilterConfiguration.getOnRejectResponse();
            } else {
                response.getHeaders().add(ipFilterConfiguration.getHeaderName(), ipFilterConfiguration.getHeaderValueOnAccepted());

            }
        }

        if (shouldFilterRequest.test(request)) {
            filterConfig.getRateLimitGuardians().forEach(rateLimitGuardian -> {
                AbstractMap.SimpleEntry<RateLimitAction, CompletableFuture<ConsumptionProbe>> consumptionProbeCompletableFuturePair = rateLimitGuardian.examine(request);

                if (consumptionProbeCompletableFuturePair.getKey() == RateLimitAction.TRUDGE) {
                    allToSkip.set(false);
                    if (consumptionProbeCompletableFuturePair.getValue() != null) {

                        CompletableFuture<ConsumptionProbe> consumptionProbeCompletableFuture = consumptionProbeCompletableFuturePair.getValue();
                        rateLimitFutures.add(consumptionProbeCompletableFuture.thenCompose(probe -> {
                            if (probe.isConsumed()) {
                                return CompletableFuture.completedFuture(probe.getRemainingTokens());
                            } else {
                                return CompletableFuture.completedFuture(null);
                            }
                        }));

                        refillTimeFutures.add(consumptionProbeCompletableFuture.thenCompose(probe -> {
                            if (probe.isConsumed()) {
                                return CompletableFuture.completedFuture(probe.getNanosToWaitForRefill());
                            } else {
                                return CompletableFuture.completedFuture(null);
                            }
                        }));
                    }
                } else {
                    anyToSkip.set(true);
                }
            });

            CompletableFuture<Long> rateLimitFuture = rateLimitFutures
                    .stream()
                    .reduce(null, smallerOperator);

            CompletableFuture<Long> refillTimeFuture = refillTimeFutures
                    .stream()
                    .reduce(null, smallerOperator);

            Long remainingLimit = null;
            if (rateLimitFuture != null) {
                remainingLimit = rateLimitFuture.join();
            }

            Long timeToRefill = null;
            if (refillTimeFuture != null) {
                timeToRefill = refillTimeFuture.join();
            }

            if (!allToSkip.get() && (remainingLimit == null || remainingLimit < 0)) {
                if (timeToRefill != null) {
                    response.getHeaders().set("RateLimit-Reset", "" + TimeUnit.NANOSECONDS.toSeconds(timeToRefill));
                }
                if (filterConfig.getFilterProperties().getBan() == BanType.PERMANENT) {
                    hazelcastInstance.getList(AccessControlAction.DENY.name()).add(request.getRemoteAddress().getAddress().getHostAddress());
                }
                return Mono.error(new RateLimitException(filterConfig.getFilterProperties().getRejectionMessage()));
            }

            if (allToSkip.get()) {
                response.getHeaders().set("RateLimit-Remaining", "n/a");
            } else {
                response.getHeaders().set("RateLimit-Remaining", "" + remainingLimit);
            }
        }
        return webFilterChain.filter(serverWebExchange);
    }

    private final BinaryOperator<CompletableFuture<Long>> smallerOperator = (a1, b1) -> {
        if (a1 == null) {
            return b1;
        }
        return a1.thenCombine(b1, (x, y) -> {
            if (x == null && y == null) {
                return null;
            }
            if (x != null && y == null) {
                return x;
            }
            if (x == null && y != null) {
                return y;
            }
            return x < y ? x : y;
        });
    };


    @Override
    public int getOrder() {
        return -filterConfig.getFilterProperties().getPriority();
    }
}
