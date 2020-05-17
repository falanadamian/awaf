package com.falana.awaf.context.configuration;

import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.context.properties.external.FilterProperties;
import com.falana.awaf.context.controls.RateLimitExclusion;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AWAFConfiguration {

    @NonNull
    private List<FilterProperties> filterProperties;

    @NonNull
    private FilterProperties bruteForceProtectionProperties;

    @NonNull
    private RateLimitExclusion rateLimitExclusion;

    @NonNull
    private Map<AccessControlAction, Set<String>> accessControlListsMap;

    @NonNull
    private Boolean sqlInjectionProtectionEnabled;
}
