package com.falana.awaf.context.controls;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RateLimit {

    private List<IPSecThroughput> ipSecThroughputs = new ArrayList<>();
    private List<String> whitelist = new ArrayList<>();
}
