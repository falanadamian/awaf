package com.falana.awaf.api.configuration;

import com.falana.awaf.context.properties.FilterPropertiesHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnClass(Endpoint.class)
@RequiredArgsConstructor
@Endpoint(id = "configuration")
public class AWAFConfigurationsEndpoint {

    private final FilterPropertiesHolder filterPropertiesHolder;

    @ReadOperation
    public Map<String, Object> firewallConfiguration() {
        return Collections.singletonMap("firewallConfiguration", filterPropertiesHolder.getFilterProperties());
    }

    @WriteOperation
    public void changeConfiguration(@Selector String filtername, @Selector String configurationQuery) {
        System.out.println(filtername);
    }
}
