package com.falana.awaf.detector;

import com.falana.awaf.context.RequestMatcher;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.function.Predicate;

public abstract class RequestPredicate implements Predicate<ServerHttpRequest> {
    RequestMatcher requestMatcher;
}
