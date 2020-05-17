package com.falana.awaf.api.converter;

import com.falana.awaf.context.controls.AccessControlAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AccessControlConverter implements Converter<String, AccessControlAction> {

    @Override
    public AccessControlAction convert(String value) {
        return AccessControlAction.valueOf(value.toUpperCase());
    }

}
