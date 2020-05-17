package com.falana.awaf.configuration;

import com.falana.awaf.context.CacheKeyRetriever;
import com.falana.awaf.context.RateLimitGuardian;
import com.falana.awaf.context.configuration.FilterConfiguration;
import com.falana.awaf.context.controls.IPSecThroughput;
import com.falana.awaf.context.controls.RateLimit;
import com.falana.awaf.context.properties.external.FilterProperties;
import com.falana.awaf.detector.RateLimitSkippingDetector;
import com.falana.awaf.restriction.RateLimitAction;
import io.github.bucket4j.*;
import io.github.bucket4j.grid.ProxyManager;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class FilterConfigurationBuilder {

    protected static FilterConfiguration buildFilterConfiguration(FilterProperties filterProperties, ProxyManager<String> proxyManager, CacheKeyRetriever cacheKeyRetriever) {
        List<RateLimitGuardian> rateLimitGuardians = filterProperties.getRateLimits().stream()
                .map(guardianForRateLimit(filterProperties, proxyManager, cacheKeyRetriever))
                .collect(Collectors.toList());

        return FilterConfiguration.builder()
                .filterProperties(filterProperties)
                .cacheKeyRetriever(cacheKeyRetriever)
                .rateLimitGuardians(rateLimitGuardians)
                .build();
    }

    private static Function<RateLimit, RateLimitGuardian> guardianForRateLimit(FilterProperties filterProperties, ProxyManager<String> proxyManager, CacheKeyRetriever cacheKeyRetriever) {
        return rateLimit -> {
            final ConfigurationBuilder configurationBuilder = configurationBuilderForRateLimit(rateLimit);

            RateLimitSkippingDetector skippingDetector = RateLimitSkippingDetector.builder()
                    .whiteListedIps(rateLimit.getWhitelist())
                    .excludedFiletypes(filterProperties.getExclusions().getFiletypes())
                    .contextPaths(filterProperties.getPaths())
                    .excludedSubpaths(filterProperties.getExclusions().getSubpaths())
                    .build();

            return (servletRequest) -> {
                log.info("[INTERCEPTED REQUEST]: HTTPMethod: {}. URL: {}, Headers: {}, Body: {}", servletRequest.getMethod(), servletRequest.getURI().toString(), servletRequest.getHeaders(), servletRequest.getBody());

                if (!skippingDetector.skip(servletRequest)) {
                    log.info("Intercepted request under rate limiting");

                    String cacheKey = cacheKeyRetriever.retrieve(servletRequest);
                    BucketConfiguration bucketConfiguration = configurationBuilder.build();
                    Bucket bucket = proxyManager.getProxy(cacheKey, bucketConfiguration);

                    return new AbstractMap.SimpleEntry<>(RateLimitAction.TRUDGE, bucket.asAsync().tryConsumeAndReturnRemaining(1));
                }
                log.info("Intercepted request without rate limiting");
                return new AbstractMap.SimpleEntry<>(RateLimitAction.SKIP, null);
            };
        };
    }

    private static ConfigurationBuilder configurationBuilderForRateLimit(RateLimit rateLimit) {
        ConfigurationBuilder configurationBuilder = Bucket4j.configurationBuilder();

        for (IPSecThroughput ipSecThroughput : rateLimit.getIpSecThroughputs()) {
            Bandwidth bandwidth = Bandwidth.classic(
                    ipSecThroughput.getCapacity(),
                    Refill.intervally(ipSecThroughput.getCapacity(), Duration.of(ipSecThroughput.getTime(), ipSecThroughput.getUnit()))
            );

            configurationBuilder = configurationBuilder.addLimit(bandwidth);
        }
        return configurationBuilder;
    }

}
