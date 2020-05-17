package com.falana.awaf.context.properties;

import com.falana.awaf.context.properties.external.FilterProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FilterPropertiesHolder {

    private List<FilterProperties> filterProperties = new ArrayList<>();

    public void addFilterProperties(FilterProperties filterConfiguration) {
        getFilterProperties().add(filterConfiguration);
    }
}
