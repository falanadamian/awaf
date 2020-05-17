package com.falana.awaf.detector;

import com.falana.awaf.context.RequestMatcher;
import com.falana.awaf.restriction.RequestMatchers;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Set;
import java.util.function.Function;

public class IpPredicates {

    public static Function<Set<String>, RequestPredicate> WHITELIST = Whitelist::new;
    public static Function<Set<String>, RequestPredicate> BLACKLIST = Blacklist::new;

    private static class Whitelist extends RequestPredicate {

        Whitelist(Set<String> ipList) {
            this.requestMatcher = RequestMatchers.IPs(ipList).orElse(RequestMatcher.EMPTY);
        }

        public boolean test(ServerHttpRequest serverHttpRequest) {
            return !this.requestMatcher.test(serverHttpRequest);
        }
    }

    private static class Blacklist extends RequestPredicate {

        Blacklist(Set<String> ipList) {
            this.requestMatcher = RequestMatchers.IPs(ipList).orElse(RequestMatcher.EMPTY);
        }

        public boolean test(ServerHttpRequest serverHttpRequest) {
            return this.requestMatcher.test(serverHttpRequest);
        }
    }

}
