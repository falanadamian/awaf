package com.falana.awaf.api.cache;

import com.falana.awaf.context.properties.FilterPropertiesHolder;
import com.falana.awaf.context.controls.AccessControlAction;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@ConditionalOnClass(Endpoint.class)
@RequiredArgsConstructor
@Slf4j
@Endpoint(id = "dynamic-cache-snapshot")
public class DynamicCacheMetricsEndpoint {

    private final HazelcastInstance hazelcastInstance;
    private final FilterPropertiesHolder filterPropertiesHolder;

    @ReadOperation
    public List<?> retrieveDynamicCache() {
        List<String> dynamicCachePotentialNames = Stream.of(AccessControlAction.values()).map(AccessControlAction::name).collect(Collectors.toList());

        return retrieveDynamicAccessControlListsCache(hazelcastInstance.getDistributedObjects(), dynamicCachePotentialNames);
    }

    private List<?> retrieveDynamicAccessControlListsCache(Collection<DistributedObject> hazelcastDistributedObjects, List<String> cachePotentialNames) {
        return hazelcastDistributedObjects
                .stream()
                .filter(distributedObject -> cachePotentialNames.contains(distributedObject.getName()))
                .map(distributedObject -> hazelcastInstance.getList(distributedObject.getName()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
