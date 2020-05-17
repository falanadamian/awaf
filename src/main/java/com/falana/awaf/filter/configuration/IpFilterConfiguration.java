package com.falana.awaf.filter.configuration;

import com.falana.awaf.detector.RequestPredicate;
import com.falana.awaf.detector.IpPredicates;
import com.falana.awaf.exception.BlacklistedException;
import com.falana.awaf.exception.WhitelistedException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import reactor.core.publisher.Mono;

import java.util.Set;

@Builder
@Getter
@AllArgsConstructor
public class IpFilterConfiguration {

    @NonNull
    private final RequestPredicate requestPredicate;

    @NonNull
    private final String headerName;

    @NonNull
    private final String headerValueOnAccepted;

    @NonNull
    private final String headerValueOnRejected;

    @NonNull
    private final Mono<Void> onRejectResponse;

    public static IpFilterConfiguration createForWhitelist(Set<String> whitelistedIps) {
        return IpFilterConfiguration.builder()
                .requestPredicate(IpPredicates.WHITELIST.apply(whitelistedIps))
                .headerName("Whitelist-Protection")
                .headerValueOnAccepted("accepted")
                .headerValueOnRejected("rejected")
                .onRejectResponse(Mono.error(new WhitelistedException()))
                .build();
    }

    public static IpFilterConfiguration createForBlacklist(Set<String> blacklistedIps) {
        return IpFilterConfiguration.builder()
                .requestPredicate(IpPredicates.BLACKLIST.apply(blacklistedIps))
                .headerName("Blacklist-Protection")
                .headerValueOnAccepted("accepted")
                .headerValueOnRejected("rejected")
                .onRejectResponse(Mono.error(new BlacklistedException()))
                .build();
    }

}
