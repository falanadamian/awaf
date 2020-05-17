package com.falana.awaf.restriction;

import com.falana.awaf.context.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestMatchers {

    private static BiFunction<List<String>, String, Pattern> pathToPatternConverter = (paths, contextPath) -> CollectionUtils.isEmpty(paths) ? null : Pattern.compile("^" + contextPath + "(" + String.join("|", paths) + ")");
    private static Function<List<String>, Pattern> filetypeToPatternConverter = (filetypes) -> CollectionUtils.isEmpty(filetypes) ? null : Pattern.compile("\\.(" + String.join("|", filetypes) + ")$");
    private static Function<Set<String>, Pattern> ipToPatternConverter = (ips) -> CollectionUtils.isEmpty(ips) ? null : Pattern.compile("^(" + String.join("|", ips) + ")$");

    public static Optional<RequestMatcher> whitelisted(List<String> whiteList) {
        return CollectionUtils.isEmpty(whiteList) ? Optional.empty() : Optional.of(
                serverHttpRequest -> whiteList.stream()
                        .map(Pattern::compile)
                        .anyMatch(pattern -> pattern.matcher(serverHttpRequest.getRemoteAddress().getAddress().getHostAddress()).matches())
        );
    }

    public static Optional<RequestMatcher> SUBPATHS(List<String> contextPaths, List<String> excludedSubpaths) {
        List<Pattern> pathPatterns = contextPaths.stream().map(contextPath -> pathToPatternConverter.apply(excludedSubpaths, contextPath)).collect(Collectors.toList());

        return CollectionUtils.isEmpty(contextPaths) || CollectionUtils.isEmpty(excludedSubpaths) ? Optional.empty() : Optional.of(
                serverHttpRequest -> pathPatterns.stream().anyMatch(pathPattern -> pathPattern.matcher(serverHttpRequest.getURI().getPath()).find())
        );
    }

    public static Optional<RequestMatcher> FILETYPES(List<String> filetypes) {
        Pattern filetypePattern = filetypeToPatternConverter.apply(filetypes);

        return CollectionUtils.isEmpty(filetypes) ? Optional.empty() : Optional.of(
                serverHttpRequest -> {
                    String extension = StringUtils.getFilenameExtension(serverHttpRequest.getURI().getPath());

                    if (Objects.nonNull(extension)) {
                        return filetypePattern.matcher(extension).find();
                    }
                    return false;
                }
        );
    }

    public static Optional<RequestMatcher> IPs(Set<String> ipList) {
        return CollectionUtils.isEmpty(ipList) ? Optional.empty() : Optional.of(
                serverHttpRequest -> ipToPatternConverter.apply(ipList).matcher(serverHttpRequest.getRemoteAddress().getAddress().getHostAddress()).matches()
        );
    }

}
