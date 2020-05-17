package com.falana.awaf.detector;

import com.falana.awaf.context.RequestMatcher;
import com.falana.awaf.restriction.RequestMatchers;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;

@Slf4j
public class RateLimitSkippingDetector {

    private final RequestMatcher whitelistedMatcher;
    private final RequestMatcher excludedSubpathsMatcher;
    private final RequestMatcher excludedFiletypesMatcher;

    private static final RequestMatcher EMPTY_REQUEST_RESTRICTION = serverHttpRequest -> false;

    @Builder
    public RateLimitSkippingDetector(List<String> whiteListedIps, List<String> excludedFiletypes, List<String> contextPaths, List<String> excludedSubpaths) {
        this.whitelistedMatcher = RequestMatchers.whitelisted(whiteListedIps).orElse(EMPTY_REQUEST_RESTRICTION);
        this.excludedSubpathsMatcher = RequestMatchers.SUBPATHS(contextPaths, excludedSubpaths).orElse(EMPTY_REQUEST_RESTRICTION);
        this.excludedFiletypesMatcher = RequestMatchers.FILETYPES(excludedFiletypes).orElse(EMPTY_REQUEST_RESTRICTION);
    }

    public boolean skip(ServerHttpRequest serverHttpRequest) {
        boolean skipRateLimiting = whitelistedMatcher.test(serverHttpRequest);
        skipRateLimiting = skipRateLimiting || excludedSubpathsMatcher.test(serverHttpRequest);
        skipRateLimiting = skipRateLimiting || excludedFiletypesMatcher.test(serverHttpRequest);

        return skipRateLimiting;
    }

}
