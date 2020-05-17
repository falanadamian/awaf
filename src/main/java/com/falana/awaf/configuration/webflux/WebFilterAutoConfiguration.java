package com.falana.awaf.configuration.webflux;

import com.falana.awaf.api.cache.DynamicCacheMetricsEndpoint;
import com.falana.awaf.api.cache.StaticCacheMetricsEndpoint;
import com.falana.awaf.api.configuration.AWAFConfigurationsEndpoint;
import com.falana.awaf.configuration.cache.CacheConfiguration;
import com.falana.awaf.configuration.cache.CacheResolver;
import com.falana.awaf.context.configuration.AWAFConfiguration;
import com.falana.awaf.context.properties.FilterPropertiesHolder;
import com.falana.awaf.context.properties.internal.SQLInjectionProperties;
import com.falana.awaf.detector.SQLInjectionDetector;
import com.falana.awaf.filter.DynamicIPFilter;
import com.falana.awaf.filter.SQLInjectionFilter;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.server.WebFilter;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(value = AWAFConfiguration.class)
@AutoConfigureAfter(value = {CacheAutoConfiguration.class, CacheConfiguration.class})
@Import(value = {AWAFConfigurationsEndpoint.class, DynamicCacheMetricsEndpoint.class, StaticCacheMetricsEndpoint.class})
public class WebFilterAutoConfiguration {

    private final AWAFConfiguration awafConfiguration;

    private final CacheResolver cacheResolver;
    private final HazelcastInstance hazelcastInstance;

    private final GenericApplicationContext context;

    private final SQLInjectionProperties sqlInjectionProperties;

    private AtomicInteger filterCount = new AtomicInteger(0);

    @Bean
    public FilterPropertiesHolder filterPropertiesHolder() {
        return new FilterPropertiesHolder();
    }

    @Bean
    public SQLInjectionDetector sqlInjectionDetector() {
        return new SQLInjectionDetector(sqlInjectionProperties);
    }

    @PostConstruct
    public void initializeFirewallFilters() {
        GlobalAccessControlFiltersInitializer.initializeFilters(awafConfiguration.getAccessControlListsMap(), context);

        awafConfiguration.getFilterProperties()
                .stream()
                .map(filterProperties -> FirewallRuleFilterConfigurationBuilder.initializeFilter(filterProperties, filterPropertiesHolder(), cacheResolver, hazelcastInstance))
                .flatMap(Collection::stream)
                .forEach(webFilter -> {
                    filterCount.incrementAndGet();
                    context.registerBean("firewallFilter" + filterCount, WebFilter.class, () -> webFilter);
                    log.info("Registered firewall rule filter. Current filter count: {}", filterCount);
                });

        Stream.of(awafConfiguration.getBruteForceProtectionProperties())
                .map(filterProperties -> FirewallRuleFilterConfigurationBuilder.initializeFilter(filterProperties, filterPropertiesHolder(), cacheResolver, hazelcastInstance))
                .flatMap(Collection::stream)
                .forEach(webFilter -> {
                    filterCount.incrementAndGet();
                    context.registerBean("firewallFilter" + filterCount, WebFilter.class, () -> webFilter);
                    log.info("Registered firewall brute force protection filter. Current filter count: {}", filterCount);
                });

        context.registerBean("dynamicIPFilter", WebFilter.class, () -> new DynamicIPFilter(hazelcastInstance));

        if (awafConfiguration.getSqlInjectionProtectionEnabled()) {
            context.registerBean("sqlInjectionFilter", WebFilter.class, () -> new SQLInjectionFilter(sqlInjectionDetector()));
        }
    }


}
