package com.falana.awaf.configuration.cache;

import com.hazelcast.core.HazelcastInstance;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.hazelcast.Hazelcast;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CacheResolver {

    private final HazelcastInstance hazelcastInstance;

    public ProxyManager<String> resolve(String bucketName) {
        return Bucket4j
                .extension(Hazelcast.class)
                .proxyManagerForMap(hazelcastInstance.getMap(bucketName));
    }
}
