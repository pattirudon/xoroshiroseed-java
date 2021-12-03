package jp.co.pattirudon.xoroshiroseed;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import jp.co.pattirudon.xoroshiroseed.matrices.BinaryMatrix;
import jp.co.pattirudon.xoroshiroseed.random.Xoroshiro;

public class SeedSolverTest {
    @Test
    public void testSingleBitsMatrixRank() {
        int n = 128;
        BinaryMatrix f = SeedSolver.singleBitsMatrix(n);
        assertEquals(f.enchelon().rank, 128);
    }

    @Test
    public void testSingleBitsMatrixGeneralizedInverse() {
        for (int i = 1; i < 256; i++) {
            BinaryMatrix f = SeedSolver.singleBitsMatrix(i);
            BinaryMatrix g = f.generalizedInverse();
            BinaryMatrix fgf = f.multiplyRight(g).multiplyRight(f);
            assertArrayEquals(f.mat, fgf.mat);
        }
    }

    private static byte[] decodeMotions(String s) {
        byte[] motions = s.getBytes();
        for (int i = 0; i < motions.length; i++) {
            motions[i] -= '0';
        }
        return motions;
    }

    @Test
    public void testSolve_120() {
        String m = "1111000010011011110011001010011000100001001010101010100011010100"
                + "11001111011000001010100110000100110010110101101111001101";
        byte[] d = decodeMotions(m);
        List<long[]> result = SeedSolver.solve(d);
        assertEquals(120, m.length());
        assertEquals(1 << 8, result.size());
        for (int i = 0; i < result.size(); i++) {
            long[] s = result.get(i);
            Xoroshiro random = new Xoroshiro(s[0], s[1]);
            byte[] _d = new byte[d.length];
            for (int j = 0; j < d.length; j++) {
                _d[j] = (byte) (random.nextInt() & 1);
            }
            assertArrayEquals(d, _d);
        }
    }

    @Test
    public void testSolve_127() {
        String m = "0111001010111011101001001000001100001000010111001000010111110110"
                + "100010011011010111100101011010101111100100010001100011110001100";
        List<long[]> result = SeedSolver.solve(decodeMotions(m));
        assertEquals(127, m.length());
        assertEquals(2, result.size());
        int count = 0;
        for (int i = 0; i < result.size(); i++) {
            if (Arrays.equals(new long[] { 0xd2440e1966cad539L, 0xa2fe6e8e5d299fb1L }, result.get(i)))
                count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testSolve_128() {
        String m = "1011000111111110110010111011111010001101010001111110101001100100"
                + "0110011111101100111110100110100110000011101010101000001101001101";
        List<long[]> result = SeedSolver.solve(decodeMotions(m));
        assertEquals(128, m.length());
        assertEquals(1, result.size());
        assertArrayEquals(new long[] { 0x913375dc527bb37eL, 0x43b4a022b5d8b9f7L }, result.get(0));
    }

    @Test
    public void testSolve_129() {
        String m = "0011111000111011100110000110101001101111000000110010010000101011"
                + "11100001111010100000010000011110101001010110100010100011100000111";
        List<long[]> result = SeedSolver.solve(decodeMotions(m));
        assertEquals(129, m.length());
        assertEquals(1, result.size());
        assertArrayEquals(new long[] { 0xeecb6ca59f4a8799L, 0x6918eddd4e253e8dL }, result.get(0));
    }

    @Test
    public void testSolve_130() {
        String m = "1100010010000100100101001011100101000100110101111101001001011001"
                + "001000111100000110110000110111101011100011101111111000110011001010";
        List<long[]> result = SeedSolver.solve(decodeMotions(m));
        assertEquals(130, m.length());
        assertEquals(0, result.size());
    }

    @Test
    public void testFindSingleState_0() {
        String m = "0111100010101001100010101000011010111011011111010000001111000010"
                + "10010111";
        int designated = 1;
        long s = Xoroshiro.XOROSHIRO_CONST;
        int frameStartInclusive = 6400;
        int frameEndExclusive = 6800;
        Entry<List<Integer>, List<long[]>> e = SeedSolver.findSingleState(decodeMotions(m), designated, s,
                frameStartInclusive, frameEndExclusive);
        List<Integer> frameMotionStart = e.getKey();
        List<long[]> stateGameStart = e.getValue();
        int i = frameMotionStart.indexOf(6596);
        assertTrue(0 <= i);
        assertArrayEquals(new long[] { 0x1ffcee5168387a1dL, Xoroshiro.XOROSHIRO_CONST }, stateGameStart.get(i));
    }

    @Test
    public void testFindSingleState_1() {
        String m = "0110010101110010111101111000010111010111110111101111110101101111"
                + "101001001011";
        int designated = 0;
        long s = 0xca4c2f63c244046cL;
        int frameStartInclusive = 60000;
        int frameEndExclusive = 60001;
        Entry<List<Integer>, List<long[]>> e = SeedSolver.findSingleState(decodeMotions(m), designated, s,
                frameStartInclusive, frameEndExclusive);
        List<Integer> frameMotionStart = e.getKey();
        List<long[]> stateGameStart = e.getValue();
        assertEquals(1, frameMotionStart.size());
        assertEquals(frameStartInclusive, frameMotionStart.get(0).intValue());
        assertEquals(1, stateGameStart.size());
        assertArrayEquals(new long[] { s, 0x08b66923c3d60eacL }, stateGameStart.get(0));
    }
}
