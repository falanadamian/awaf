package com.falana.awaf.context.properties.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@ConfigurationProperties(prefix = SQLInjectionProperties.PROPERTY_PREFIX)
public class SQLInjectionProperties {

    public static final String PROPERTY_PREFIX = "sql-injection";

    private List<String> commands;
    private List<String> patterns;

    private String rejectionMessage;
}
