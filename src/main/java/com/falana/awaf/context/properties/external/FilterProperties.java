package com.falana.awaf.context.properties.external;

import com.falana.awaf.context.controls.BanType;
import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.context.controls.RateLimit;
import com.falana.awaf.context.controls.RateLimitExclusion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Getter
@Setter
@Validated
@NoArgsConstructor
public class FilterProperties implements Validator {

    private String name;

    private BanType ban = BanType.TEMPORARY;

    private List<String> paths = new ArrayList<>();

    private int priority = 0;

    private List<RateLimit> rateLimits = new ArrayList<>();

    private Map<AccessControlAction, Set<String>> accessControl = new HashMap<>();

    private String rejectionMessage = "Too many request sent, you have been blocked";

    private RateLimitExclusion exclusions = new RateLimitExclusion();

    @PostConstruct
    public void revertPriority() {
        priority *= -1;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == FilterProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        FilterProperties filterProperties = (FilterProperties) target;

        if (StringUtils.isEmpty(filterProperties.name)) {
            errors.rejectValue("name", "name", "Rule name must be specified.");
        }

        filterProperties.paths.forEach(path -> {
            if (!isValidPath(path)) {
                errors.rejectValue("path", "path", "Invalid path pattern found, correct defined paths.");
            }
        });

        if (priority < 0) {
            errors.rejectValue("priority", "priority", "Rule priority cannot be a negative number.");
        }

        if (!CollectionUtils.isEmpty(filterProperties.accessControl.get(AccessControlAction.PERMIT)) && !CollectionUtils.isEmpty(filterProperties.accessControl.get(AccessControlAction.DENY))) {
            errors.rejectValue("blacklist", "blacklist", "IP address blacklist cannot be defined when IP address whitelist is present");
        }
    }

    private boolean isValidPath(String path) {
        try {
            Pattern.compile(path);
            if (path.equals("/*")) {
                return false;
            }
        } catch (PatternSyntaxException exception) {
            return false;
        }
        return true;
    }

}