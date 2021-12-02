package jp.co.pattirudon.xoroshiroseed.matrices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinaryMatrix {
    public final int rows;
    public final int columns;
    public final byte[][] mat;

    private BinaryMatrix(int rows, int columns, byte[][] mat) {
        this.rows = rows;
        this.columns = columns;
        this.mat = mat;
    }

    public static BinaryMatrix getInstance(int rows, int columns, byte[][] mat, boolean copy) {
        if (copy) {
            byte[][] _mat = new byte[rows][];
            for (int i = 0; i < rows; i++) {
                _mat[i] = Arrays.copyOf(mat[i], columns);
            }
            return new BinaryMatrix(rows, columns, _mat);
        } else {
            return new BinaryMatrix(rows, columns, mat);
        }
    }

    private static BinaryMatrix getInstance(BinaryMatrix binaryMatrix, boolean copy) {
        return BinaryMatrix.getInstance(binaryMatrix.rows, binaryMatrix.columns, binaryMatrix.mat, copy);
    }

    public static BinaryMatrix ones(int n) {
        byte[][] mat = new byte[n][n];
        for (int i = 0; i < n; i++) {
            mat[i][i] = (byte) 1;
        }
        BinaryMatrix o = new BinaryMatrix(n, n, mat);
        return o;
    }

    public BinaryMatrix transposed() {
        byte[][] t = new byte[columns][rows];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                t[i][j] = mat[j][i];
            }
        }
        return new BinaryMatrix(columns, rows, t);
    }

    public void swapRows(int i, int j) {
        byte[] ri = mat[i];
        byte[] rj = mat[j];
        mat[i] = rj;
        mat[j] = ri;
    }

    public void addRows(int src, int dst) {
        for (int j = 0; j < columns; j++) {
            mat[dst][j] ^= mat[src][j];
        }
    }

    public BinaryMatrix resized(int newRows) {
        byte[][] newMat = new byte[newRows][];
        for (int i = 0; i < Math.min(this.rows, newRows); i++) {
            newMat[i] = Arrays.copyOf(this.mat[i], this.columns);
        }
        for (int i = this.rows; i < newRows; i++) {
            newMat[i] = new byte[this.columns];
        }
        return new BinaryMatrix(newRows, this.columns, newMat);
    }

    public byte[] multiplyRight(byte[] column) {
        if (columns != column.length) {
            throw new IllegalColumnCountException(
                    "The length of the column vector must equal to the number of the columns of this matrix");
        }
        byte[] result = new byte[rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i] ^= mat[i][j] & column[j];
            }
        }
        return result;
    }

    public byte[] multiplyLeft(byte[] row) {
        if (rows != row.length) {
            throw new IllegalRowCountException(
                    "The length of the row vector must equal to the number of the rows of this matrix");
        }
        byte[] result = new byte[columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j] ^= row[i] & mat[i][j];
            }
        }
        return result;
    }

    public BinaryMatrix multiplyRight(BinaryMatrix another) {
        if (this.columns != another.rows) {
            throw new IllegalColumnCountException(
                    "The number of the rows of another matrix must equal to the number of the columns of this matrix");
        }
        byte[][] _mat = new byte[this.rows][];
        for (int i = 0; i < this.rows; i++) {
            _mat[i] = another.multiplyLeft(this.mat[i]);
        }
        return new BinaryMatrix(this.rows, another.columns, _mat);
    }

    public BinaryMatrix add(BinaryMatrix another) {
        if (this.rows != another.rows) {
            throw new IllegalRowCountException(
                    "The two matrices must have same number of rows.");
        } else if (this.columns != another.columns) {
            throw new IllegalColumnCountException(
                    "The two matrices must have same number of columns.");
        }
        byte[][] _mat = new byte[this.rows][this.columns];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                _mat[i][j] = (byte) (this.mat[i][j] ^ another.mat[i][j]);
            }
        }
        return new BinaryMatrix(this.rows, this.columns, _mat);
    }

    /**
     * 階段行列を返す．thisに変更は加えない．
     * @return 
     */
    public Enchelon enchelon() {
        BinaryMatrix f = BinaryMatrix.getInstance(this, true);
        BinaryMatrix p = BinaryMatrix.ones(rows);
        int rank = 0;
        List<Integer> pivotsList = new ArrayList<>();
        for (int j = 0; j < f.columns; j++) {
            for (int i = rank; i < f.rows; i++) {
                if (f.mat[i][j] != 0) {
                    /* erase other rows */
                    for (int k = 0; k < f.rows; k++) {
                        if ((k != i) && (f.mat[k][j] != 0)) {
                            f.addRows(i, k);
                            p.addRows(i, k);
                        }
                    }
                    f.swapRows(i, rank);
                    p.swapRows(i, rank);
                    pivotsList.add(j);
                    rank++;
                    break;
                }
            }
        }
        return new Enchelon(f, p, rank, pivotsList);
    }

    public BinaryMatrix generalizedInverse() {
        Enchelon e = enchelon();
        BinaryMatrix p = e.p;
        int rank = e.rank;
        List<Integer> pivots = e.pivots;
        BinaryMatrix permp = p.resized(this.columns);
        for (int i = rank - 1; i >= 0; i--) {
            int columnIndex = pivots.get(i);
            permp.swapRows(i, columnIndex);
        }
        return permp;
    }

    public byte[][] rowBasis() {
        Enchelon e = enchelon();
        BinaryMatrix f = e.f;
        return Arrays.copyOf(f.mat, e.rank);
    }

    public static class Enchelon {
        public final BinaryMatrix f;
        public final BinaryMatrix p;
        public final int rank;
        public final List<Integer> pivots;

        Enchelon(BinaryMatrix f, BinaryMatrix p, int rank, List<Integer> pivots) {
            this.f = f;
            this.p = p;
            this.rank = rank;
            this.pivots = pivots;
        }
    }
}
