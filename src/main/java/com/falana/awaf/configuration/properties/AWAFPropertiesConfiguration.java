package com.falana.awaf.configuration.properties;

import com.falana.awaf.configuration.cache.CacheResolver;
import com.falana.awaf.context.configuration.AWAFConfiguration;
import com.falana.awaf.context.properties.external.AWAFProperties;
import com.falana.awaf.context.properties.internal.SQLInjectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AWAFProperties.class, SQLInjectionProperties.class})
@ConditionalOnProperty(prefix = AWAFProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@ConditionalOnBean(value = CacheResolver.class)
@RequiredArgsConstructor
public class AWAFPropertiesConfiguration {

    private final AWAFProperties awafProperties;

    @Bean
    public AWAFConfiguration awafConfiguration() {
        return AWAFConfiguration.builder()
                .filterProperties(awafProperties.getRules())
                .bruteForceProtectionProperties(awafProperties.getBruteForceProtection())
                .accessControlListsMap(awafProperties.getAccessControl())
                .rateLimitExclusion(awafProperties.getExclusions())
                .sqlInjectionProtectionEnabled(awafProperties.getSqlInjection())
                .build();
    }

    @Bean
    public SQLInjectionProperties sqlInjectionProperties() {
        return new SQLInjectionProperties();
    }
}
