package com.falana.awaf.detector;

import com.falana.awaf.context.properties.internal.SQLInjectionProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Slf4j
@Getter
public class SQLInjectionDetector {

    private final SQLInjectionProperties properties;

    public SQLInjectionDetector(SQLInjectionProperties properties) {
        this.properties = properties;
    }

    public boolean detect(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return containsSQLCommand(input) || containsMaliciousText(input);
    }

    private boolean containsSQLCommand(String input) {
        log.info("Input under test for SQL commands presence");

        boolean containsSQLCommand = false;

        final String lowerCaseInput = input.toLowerCase();

        for (String command : properties.getCommands()) {
            if (lowerCaseInput.contains(command)) {
                log.info("SQL command '{}' found in input: {}", input);
                containsSQLCommand = true;
                break;
            }
        }
        return containsSQLCommand;
    }

    private boolean containsMaliciousText(String input) {
        log.info("Input under test for malicious SQL");

        final String lowerCaseInput = input.toLowerCase();

        return properties.getPatterns().stream().map(Pattern::compile).anyMatch(pattern -> {

            boolean matches = pattern.matcher(lowerCaseInput).find();

            if (matches) {
                log.info("Found malicious input for {} pattern: {}", pattern, lowerCaseInput);
            }
            return matches;
        });
    }
}
