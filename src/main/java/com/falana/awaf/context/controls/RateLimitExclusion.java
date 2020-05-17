package com.falana.awaf.context.controls;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RateLimitExclusion {
    private List<String> filetypes;
    private List<String> subpaths;
}
