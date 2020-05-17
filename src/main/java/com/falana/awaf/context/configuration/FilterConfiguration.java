package com.falana.awaf.context.configuration;

import com.falana.awaf.context.CacheKeyRetriever;
import com.falana.awaf.context.RateLimitGuardian;
import com.falana.awaf.context.properties.external.FilterProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class FilterConfiguration {

    private final FilterProperties filterProperties;
    private final List<RateLimitGuardian> rateLimitGuardians;
    private final CacheKeyRetriever cacheKeyRetriever;
}
