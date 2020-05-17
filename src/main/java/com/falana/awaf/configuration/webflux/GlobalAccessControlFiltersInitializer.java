package com.falana.awaf.configuration.webflux;

import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.filter.IpFilter;
import com.falana.awaf.filter.configuration.IpFilterConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.WebFilter;

import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
class GlobalAccessControlFiltersInitializer {

    static void initializeFilters(Map<AccessControlAction, Set<String>> globalAccessControlListsMap, GenericApplicationContext context) {
        if (isDenyAccessControlListPresent(globalAccessControlListsMap)) {
            initializeGlobalDenyAccessControlListFilter(globalAccessControlListsMap.get(AccessControlAction.DENY), context);
        }

        if (isPermitAccessControlListPresent(globalAccessControlListsMap)) {
            initializeGlobalPermitAccessControlListFilter(globalAccessControlListsMap.get(AccessControlAction.PERMIT), context);
        }
    }

    private static boolean isDenyAccessControlListPresent(Map<AccessControlAction, Set<String>> accessControlListsMap) {
        return !CollectionUtils.isEmpty(accessControlListsMap.get(AccessControlAction.DENY));
    }

    private static boolean isPermitAccessControlListPresent(Map<AccessControlAction, Set<String>> accessControlListsMap) {
        return !CollectionUtils.isEmpty(accessControlListsMap.get(AccessControlAction.PERMIT));
    }

    private static void initializeGlobalDenyAccessControlListFilter(Set<String> accessControlList, GenericApplicationContext context) {
        WebFilter blacklistedFilter = new IpFilter(
                IpFilterConfiguration.createForBlacklist(accessControlList)
        );

        context.registerBean("denyAccessControlListFilter", WebFilter.class, () -> blacklistedFilter);

        log.info("Registered DENY access control list filter");
    }

    private static void initializeGlobalPermitAccessControlListFilter(Set<String> accessControlList, GenericApplicationContext context) {
        WebFilter whitelistedFilter = new IpFilter(
                IpFilterConfiguration.createForWhitelist(accessControlList)
        );

        context.registerBean("permitAccessControlListFilter", WebFilter.class, () -> whitelistedFilter);

        log.info("Registered PERMIT access control list filter");
    }
}
