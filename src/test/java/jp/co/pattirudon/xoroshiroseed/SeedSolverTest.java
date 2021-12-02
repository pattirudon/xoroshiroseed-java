package jp.co.pattirudon.xoroshiroseed;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jp.co.pattirudon.xoroshiroseed.matrices.BinaryMatrix;

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
}
