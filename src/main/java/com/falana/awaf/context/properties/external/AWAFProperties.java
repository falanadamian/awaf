package com.falana.awaf.context.properties.external;

import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.context.controls.RateLimitExclusion;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = AWAFProperties.PROPERTY_PREFIX)
public class AWAFProperties implements Validator {

    public static final String PROPERTY_PREFIX = "awaf";

    private Boolean enabled = true;

    private List<FilterProperties> rules = new ArrayList<>();

    private FilterProperties bruteForceProtection;

    private RateLimitExclusion exclusions = new RateLimitExclusion();

    private Map<AccessControlAction, Set<String>> accessControl;

    private Boolean sqlInjection = false;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == AWAFProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        AWAFProperties awafProperties = (AWAFProperties) target;

        if (!CollectionUtils.isEmpty(awafProperties.getAccessControl().get(AccessControlAction.PERMIT)) && !CollectionUtils.isEmpty(awafProperties.getAccessControl().get(AccessControlAction.DENY))) {
            errors.rejectValue("blacklist", "DENY access control cannot be defined when PERMIT access control is present. Delete one of access controls.");
        }

        awafProperties.rules.forEach(filterProperties -> {
            errors.pushNestedPath(String.format("rules[%s]", filterProperties.getName()));
            filterProperties.validate(filterProperties, errors);
            errors.popNestedPath();
        });
    }
}
