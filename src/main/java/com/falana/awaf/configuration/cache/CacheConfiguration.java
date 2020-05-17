package com.falana.awaf.configuration.cache;

import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.BinaryOperator;

@Configuration
@ConditionalOnBean(HazelcastInstance.class)
@AutoConfigureAfter(CacheAutoConfiguration.class)
@RequiredArgsConstructor
public class CacheConfiguration {

    public static final String CACHE_KEY_SEPARATOR = "/";
    public static final String CACHE_KEY_FORMAT = "%s" + CACHE_KEY_SEPARATOR + "%s";
    public static final BinaryOperator<String> CACHE_KEY_CALCULATOR = (ip, path) -> String.format(CACHE_KEY_FORMAT, ip, path);

    private final HazelcastInstance hazelcastInstance;

    @Bean
    public CacheResolver hazelcastCacheResolver() {
        return new CacheResolver(hazelcastInstance);
    }
}