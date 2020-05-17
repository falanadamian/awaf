package com.falana.awaf.api.cache;

import com.falana.awaf.context.properties.FilterPropertiesHolder;
import com.falana.awaf.context.properties.external.FilterProperties;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import io.github.bucket4j.grid.GridBucketState;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.falana.awaf.configuration.cache.CacheConfiguration.CACHE_KEY_SEPARATOR;

@Configuration
@Endpoint(id = "static-cache-snapshot")
@RequiredArgsConstructor
@ConditionalOnClass(Endpoint.class)
public class StaticCacheMetricsEndpoint {

    private final HazelcastInstance hazelcastInstance;
    private final FilterPropertiesHolder filterPropertiesHolder;


    @ReadOperation
    public Map<String, Map<String, Long>> retrieveStaticCache() {
        List<String> filterMapNames = filterPropertiesHolder.getFilterProperties().stream().map(FilterProperties::getName).collect(Collectors.toList());

        return assembleIpPathsToAvailableTokensCacheMap(hazelcastInstance.getDistributedObjects(), filterMapNames);
    }

    private Map<String, Map<String, Long>> assembleIpPathsToAvailableTokensCacheMap(Collection<DistributedObject> hazelcastDistributedObjects, List<String> cachePotentialNames) {
        Map<String, Map<String, Long>> resultMap = new HashMap<>();

        hazelcastDistributedObjects
                .stream()
                .filter(distributedObject -> cachePotentialNames.contains(distributedObject.getName()))
                .map(distributedObject -> hazelcastInstance.getMap(distributedObject.getName()))
                .forEach(map -> map.forEach((key, value) -> {
                    String[] cacheKeyParts = ((String) key).split(CACHE_KEY_SEPARATOR, 2);
                    String ipAddress = cacheKeyParts[0];
                    String path = cacheKeyParts[1];

                    if (resultMap.containsKey(ipAddress)) {
                        resultMap.get(ipAddress).put(path, ((GridBucketState) value).getAvailableTokens());
                    } else {
                        resultMap.put(ipAddress, new HashMap<>(Map.of(path, ((GridBucketState) value).getAvailableTokens())));
                    }
                }));
        return resultMap;
    }
}