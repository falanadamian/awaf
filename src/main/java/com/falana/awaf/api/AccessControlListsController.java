package com.falana.awaf.api;

import com.falana.awaf.context.configuration.AWAFConfiguration;
import com.falana.awaf.context.controls.AccessControlAction;
import com.falana.awaf.exception.AlreadyAnAccessControlListMemberException;
import com.falana.awaf.exception.InvalidIpAddressException;
import com.falana.awaf.exception.NotAnAccessControlListMemberException;
import com.falana.awaf.exception.NotFoundException;
import com.fasterxml.jackson.databind.node.TextNode;
import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("access-control-lists/{accessControlList}")
public class AccessControlListsController {

    private final HazelcastInstance hazelcastInstance;
    private final AWAFConfiguration awafConfiguration;

    @GetMapping("/ips")
    public List<?> getAccessControlListIps(@PathVariable AccessControlAction accessControlActionList) {
        log.debug("GET {} access control list IPs", accessControlActionList);
        if (!isAccessControlActive(accessControlActionList)) {
            throw new NotFoundException("Inactive access control.");
        }
        return Stream.concat(
                hazelcastInstance.getList(accessControlActionList.name()).stream(),
                awafConfiguration.getAccessControlListsMap().get(accessControlActionList).stream()
        ).collect(Collectors.toList());
    }

    @PostMapping("/ips")
    @ResponseStatus(HttpStatus.CREATED)
    public void addIpToAccessControlList(@PathVariable AccessControlAction accessControlActionList, @RequestBody TextNode ip) {
        log.debug("POST {} IP on {} access control list", ip.asText(), accessControlActionList);
        if (!isAccessControlActive(accessControlActionList)) {
            throw new NotFoundException("Inactive access control.");
        }
        if (!InetAddressValidator.getInstance().isValid(ip.asText())) {
            throw new InvalidIpAddressException(String.format("IP address: %s is invalid", ip.asText()));
        }
        if (awafConfiguration.getAccessControlListsMap().get(accessControlActionList).contains(ip.asText())) {
            throw new AlreadyAnAccessControlListMemberException("Ip is already a member of access control list.");
        }
        this.awafConfiguration.getAccessControlListsMap().get(accessControlActionList).add(ip.asText());
    }

    @DeleteMapping("/ips/{ip}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeIpFromAccessControlList(@PathVariable AccessControlAction accessControlActionList, @PathVariable String ip) {
        log.debug("DELETE {} IP on {} access control list", ip, accessControlActionList);
        if (!isAccessControlActive(accessControlActionList)) {
            throw new NotFoundException("Inactive access control.");
        }
        if (!InetAddressValidator.getInstance().isValid(ip)) {
            throw new InvalidIpAddressException(String.format("IP address: %s is invalid", ip));
        }
        if (!awafConfiguration.getAccessControlListsMap().get(accessControlActionList).contains(ip)) {
            throw new NotAnAccessControlListMemberException("Ip is not a member of access control list.");
        }
        this.awafConfiguration.getAccessControlListsMap().get(accessControlActionList).removeIf(acIp -> acIp.equals(ip));
    }

    private boolean isAccessControlActive(AccessControlAction accessControlAction) {
        return awafConfiguration.getAccessControlListsMap().containsKey(accessControlAction);
    }

}
