package jp.co.pattirudon.xoroshiroseed.config;

import java.util.OptionalLong;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SeedSolverConfig {
    public byte[] motions;
    public OptionalLong s0, s1;
    public FrameConfig frame;

    protected void setMotions(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Motions cannot be null.");
        } else if (s.isEmpty()) {
            throw new IllegalArgumentException("Motions cannot be empty");
        } else {
            motions = new byte[s.length()];
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '0')
                    motions[i] = (byte) 0;
                else if (s.charAt(i) == '1')
                    motions[i] = (byte) 1;
                else
                    throw new IllegalArgumentException(
                            String.format("Illegal motion '%c' found at index %d.", s.charAt(i), i));
            }
        }
    }

    protected void setS0(String s0) {
        this.s0 = toUnsignedOptionalLong(s0);
    }

    protected void setS1(String s1) {
        this.s1 = toUnsignedOptionalLong(s1);
    }

    public static OptionalLong toUnsignedOptionalLong(String s) {
        if (s == null) {
            return OptionalLong.empty();
        } else {
            return OptionalLong.of(Long.parseUnsignedLong(s, 16));
        }
    }

    public SeedSolverConfig(@JsonProperty(value = "motions", required = true) String s,
            @JsonProperty(value = "s0") String s0, @JsonProperty(value = "s1") String s1,
            @JsonProperty(value = "frame") FrameConfig frame) {
        setMotions(s);
        setS0(s0);
        setS1(s1);
        this.frame = frame;
    }
}
