package com.falana.awaf.configuration.webflux;

import com.falana.awaf.configuration.FilterConfigurationBuilder;
import com.falana.awaf.configuration.cache.CacheResolver;
import com.falana.awaf.context.configuration.FilterConfiguration;
import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.context.properties.FilterPropertiesHolder;
import com.falana.awaf.context.properties.external.FilterProperties;
import com.falana.awaf.filter.WebfluxWebFilter;
import com.falana.awaf.filter.configuration.IpFilterConfiguration;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.WebFilter;

import java.util.List;
import java.util.stream.Collectors;

import static com.falana.awaf.configuration.cache.CacheConfiguration.CACHE_KEY_CALCULATOR;

@Slf4j
@RequiredArgsConstructor
class FirewallRuleFilterConfigurationBuilder extends FilterConfigurationBuilder {

    static List<WebFilter> initializeFilter(final FilterProperties filterProperties, final FilterPropertiesHolder filterPropertiesHolder, final CacheResolver cacheResolver, final HazelcastInstance hazelcastInstance) {
        filterPropertiesHolder.addFilterProperties(filterProperties);

        return filterProperties.getPaths().stream().map(path -> {
            FilterConfiguration filterConfig = buildFilterConfiguration(
                    filterProperties,
                    cacheResolver.resolve(filterProperties.getName()),
                    (request) -> CACHE_KEY_CALCULATOR.apply(request.getRemoteAddress().getAddress().getHostAddress(), path)
            );

            IpFilterConfiguration ipFilterConfiguration = CollectionUtils.isEmpty(filterProperties.getAccessControl().get(AccessControlAction.PERMIT)) ?
                    (CollectionUtils.isEmpty(filterProperties.getAccessControl().get(AccessControlAction.DENY)) ? null : IpFilterConfiguration.createForBlacklist(filterProperties.getAccessControl().get(AccessControlAction.DENY)))
                    : IpFilterConfiguration.createForWhitelist(filterProperties.getAccessControl().get(AccessControlAction.PERMIT));

            WebFilter webFilter = WebfluxWebFilter.builder()
                    .hazelcastInstance(hazelcastInstance)
                    .filterConfig(filterConfig)
                    .ipFilterConfiguration(ipFilterConfiguration)
                    .shouldFilterRequest((serverHttpRequest -> serverHttpRequest.getURI().getPath().matches(path)))
                    .build();


            log.info("Initialized {} filter on {} path", filterProperties.getName(), path);
            return webFilter;
        }).collect(Collectors.toList());
    }
}
