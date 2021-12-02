package jp.co.pattirudon.xoroshiroseed.random;

public class Xoroshiro {
    public static final long XOROSHIRO_CONST = 0x82a2b175229d6a5bL;
    public int i = 0;

    public final long[] s = { 0L, 0L };

    static long rotl(long x, int k) {
        return (x << k) | (x >>> (64 - k));
    }

    public Xoroshiro(long seed) {
        s[0] = seed;
        s[1] = XOROSHIRO_CONST;
    }

    public Xoroshiro(long s0, long s1) {
        s[0] = s0;
        s[1] = s1;
    }

    public long next() {
        long s0 = s[0];
        long s1 = s[1];
        long result = s0 + s1;

        s1 ^= s0;
        s[0] = rotl(s0, 24) ^ s1 ^ (s1 << 16);
        s[1] = rotl(s1, 37);
        i++;
        return result;
    }

    public long privious() {
        long t0 = s[0];
        long t1 = s[1];
        t1 = rotl(t1, 27);
        t0 = t0 ^ t1 ^ (t1 << 16);
        t0 = rotl(t0, 40);
        t1 ^= t0;
        s[0] = t0;
        s[1] = t1;
        long result = t0 + t1;
        return result;
    }

    public int nextInt() {
        return (int) next();
    }

    public static long getMask(long x) {
        x--;
        for (int i = 0; i < 64; i++) {
            x |= x >>> (1 << i);
        }
        return x;
    }

    public long random(long N) {
        long mask = getMask(N);
        long result;
        while (true) {
            result = next() & mask;
            if (result < N) {
                return result;
            }
        }
    }
}
