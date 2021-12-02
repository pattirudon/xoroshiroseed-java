package jp.co.pattirudon.xoroshiroseed.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FrameConfig {
    public int startInclusive, endExclusive;

    @JsonCreator
    public FrameConfig(@JsonProperty(value = "startInclusive", required = true) int startInclusive,
            @JsonProperty(value = "endExclusive", required = true) int endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }
}