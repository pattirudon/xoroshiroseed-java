package jp.co.pattirudon.xoroshiroseed;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jp.co.pattirudon.xoroshiroseed.config.SeedSolverConfig;
import jp.co.pattirudon.xoroshiroseed.matrices.BinaryMatrix;
import jp.co.pattirudon.xoroshiroseed.random.Xoroshiro;

public class SeedSolver {
    static BinaryMatrix singleBitsMatrix(int n) {
        byte[][] mat = new byte[128][n];
        for (int i = 0; i < 128; i++) {
            long s0 = 0;
            long s1 = 0;
            if (i < 64) {
                s0 = 1L << i;
            } else {
                s1 = 1L << (i - 64);
            }
            Xoroshiro random = new Xoroshiro(s0, s1);
            for (int j = 0; j < n; j++) {
                mat[i][j] = (byte) (random.nextInt() & 1);
            }
        }
        return BinaryMatrix.getInstance(128, n, mat, false);
    }

    public static List<long[]> solve(byte[] motions) {
        BinaryMatrix f = singleBitsMatrix(motions.length);
        BinaryMatrix g = f.generalizedInverse();
        byte[] base = g.multiplyLeft(motions);
        byte[] checkMotions = f.multiplyLeft(base);
        if (Arrays.equals(motions, checkMotions)) {
            BinaryMatrix h = f.multiplyRight(g).add(BinaryMatrix.ones(128));
            byte[][] nullBasis = h.rowBasis();
            int nullRank = nullBasis.length;
            if (nullRank >= 16)
                throw new IllegalStateException("Too less motions. Being not less than 128 recommended.");
            long[] baseLongs = toUnsignedLongArrayBE(base);
            long[][] nullLongBasis = new long[nullRank][];
            for (int i = 0; i < nullLongBasis.length; i++) {
                nullLongBasis[i] = toUnsignedLongArrayBE(nullBasis[i]);
            }
            List<long[]> affine = new ArrayList<>(1 << nullRank);
            for (int i = 0; i < (1 << nullRank); i++) {
                long[] p = Arrays.copyOf(baseLongs, baseLongs.length);
                affine.add(p);
                for (int j = 0; j < nullRank; j++) {
                    int b = (i >>> j) & 1;
                    if (b == 1) {
                        for (int k = 0; k < p.length; k++) {
                            p[k] ^= nullLongBasis[j][k];
                        }
                    }
                }
            }
            return affine;
        } else {
            return new ArrayList<long[]>(0);
        }
    }

    private static long toUnsignedLongBE(byte[] b) {
        if (b.length != 64) {
            throw new IllegalArgumentException("The lenght of byte array must be 64.");
        }
        long n = 0L;
        for (int i = 0; i < 64; i++) {
            n |= (1L & b[i]) << i;
        }
        return n;
    }

    private static long[] toUnsignedLongArrayBE(byte[] b) {
        if (b.length % 64 != 0) {
            throw new IllegalArgumentException("The lenght of byte array must be a multiple of 64.");
        }
        long[] a = new long[b.length / 64];
        for (int i = 0; i < a.length; i++) {
            byte[] sub = Arrays.copyOfRange(b, 64 * i, 64 * (i + 1));
            a[i] = toUnsignedLongBE(sub);
        }
        return a;
    }

    public static List<Integer> findMotionStartFrames(byte[] motions, long s0, long s1, int frameStartInclusive,
            int frameEndExclusive) {
        Byte[] motionsBoxed = new Byte[motions.length];
        Arrays.setAll(motionsBoxed, i -> motions[i]);
        Xoroshiro random = new Xoroshiro(s0, s1);
        for (int i = 0; i < frameStartInclusive; i++) {
            random.nextInt();
        }
        LinkedList<Byte> _motions = new LinkedList<>();
        List<Integer> foundFrames = new ArrayList<>();
        for (int i = frameStartInclusive; i < frameEndExclusive + motionsBoxed.length; i++) {
            if (motionsBoxed.length <= _motions.size()) {
                if (Arrays.equals(motionsBoxed, _motions.toArray(Byte[]::new))) {
                    foundFrames.add(i - motionsBoxed.length);
                }
                _motions.removeFirst();
            }
            byte b = (byte) (random.nextInt() & 1);
            _motions.add(b);
        }
        return foundFrames;
    }

    public static Entry<List<Integer>, List<long[]>> findSingleState(byte[] motions, int designated, long s,
            int frameStartInclusive,
            int frameEndExclusive) {
        List<long[]> motionStart = solve(motions);
        List<long[]> gameStart = new ArrayList<>();
        List<Integer> frameMotionStart = new ArrayList<>();
        for (int i = 0; i < motionStart.size(); i++) {
            long[] t = motionStart.get(i);
            Xoroshiro random = new Xoroshiro(t[0], t[1]);
            for (int j = 0; j < frameStartInclusive; j++) {
                random.privious();
            }
            for (int j = frameStartInclusive; j < frameEndExclusive; j++) {
                if (random.s[designated] == s) {
                    gameStart.add(Arrays.copyOf(random.s, random.s.length));
                    frameMotionStart.add(j);
                }
                random.privious();
            }
        }
        return new SimpleImmutableEntry<>(frameMotionStart, gameStart);
    }

    public static void list(SeedSolverConfig config, Logger logger) {
        if (config.s0.isPresent()) {
            if (config.s1.isPresent()) {
                List<Integer> motionStartFrame = findMotionStartFrames(config.motions, config.s0.getAsLong(),
                        config.s1.getAsLong(),
                        config.frame.startInclusive, config.frame.endExclusive);
                List<long[]> gameStart = Stream
                        .generate(() -> new long[] { config.s0.getAsLong(), config.s1.getAsLong() })
                        .limit(motionStartFrame.size())
                        .toList();
                print(logger, gameStart, motionStartFrame, config.motions.length);
            } else {
                int designated = 0;
                Entry<List<Integer>, List<long[]>> e = findSingleState(config.motions, designated,
                        config.s0.getAsLong(), config.frame.startInclusive, config.frame.endExclusive);
                List<Integer> motionStartFrame = e.getKey();
                List<long[]> gameStart = e.getValue();
                print(logger, gameStart, motionStartFrame, config.motions.length);
            }
        } else {
            if (config.s1.isPresent()) {
                int designated = 1;
                Entry<List<Integer>, List<long[]>> e = findSingleState(config.motions, designated,
                        config.s1.getAsLong(), config.frame.startInclusive, config.frame.endExclusive);
                List<Integer> motionStartFrame = e.getKey();
                List<long[]> gameStart = e.getValue();
                print(logger, gameStart, motionStartFrame, config.motions.length);
            } else {
                List<long[]> motionStart = solve(config.motions);
                List<Integer> motionStartFrame = Stream.<Integer>generate(() -> 0).limit(motionStart.size()).toList();
                print(logger, motionStart, motionStartFrame, config.motions.length);
            }
        }
    }

    public static void print(Logger logger, List<long[]> gameStart, List<Integer> motionStartFrame,
            int motionLength) {
        List<long[]> motionStart = new ArrayList<>(gameStart.size());
        for (int i = 0; i < gameStart.size(); i++) {
            long[] g = gameStart.get(i);
            Xoroshiro random = new Xoroshiro(g[0], g[1]);
            for (int j = 0; j < motionStartFrame.get(i); j++) {
                random.next();
            }
            motionStart.add(random.s);
        }
        List<long[]> motionEnd = new ArrayList<>(motionStart.size());
        for (int i = 0; i < motionStart.size(); i++) {
            long[] s = motionStart.get(i);
            Xoroshiro random = new Xoroshiro(s[0], s[1]);
            for (int j = 0; j < motionLength; j++) {
                random.next();
            }
            motionEnd.add(random.s);
        }
        logger.config(String.format("%d %s found.", gameStart.size(), gameStart.size() == 1 ? "was" : "were"));
        if (gameStart.size() > 0)
            logger.config(
                    "{state game started in}, {frame motion started at}, {state motion started in}, "
                            + "{frame motion ended at}, {state motion ended at}");
        int printLength = 30;
        for (int i = 0; i < gameStart.size() && i < printLength; i++) {
            long[] g = gameStart.get(i);
            long[] s = motionStart.get(i);
            long[] e = motionEnd.get(i);
            int f = motionStartFrame.get(i);
            List<String> entries = new ArrayList<>();
            entries.add(String.format("(%016x, %016x)", g[0], g[1]));
            entries.add(String.format("%d", f));
            entries.add(String.format("(%016x, %016x)", s[0], s[1]));
            entries.add(String.format("%d", f + motionLength));
            entries.add(String.format("(%016x, %016x)", e[0], e[1]));
            logger.info(String.join(", ", entries));
        }
        if (printLength < gameStart.size()) {
            logger.config("The remaining candidates are omitted.");
        }
    }
}
