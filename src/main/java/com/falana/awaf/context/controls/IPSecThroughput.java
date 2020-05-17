package com.falana.awaf.context.controls;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Validated
public class IPSecThroughput {

    private long capacity;
    private long time;
    private ChronoUnit unit;
}
