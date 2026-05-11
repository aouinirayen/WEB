package tn.esprit.pidev.ai.math;

import java.util.Random;
import java.util.function.Function;

public class Matrix {

    private double[][] data;
    private int rows;
    private int cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public Matrix(double[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, cols);
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public double get(int r, int c) { return data[r][c]; }
    public void set(int r, int c, double val) { data[r][c] = val; }
    public double[][] getData() { return data; }

    public static Matrix multiply(Matrix a, Matrix b) {
        if (a.cols != b.rows) {
            throw new IllegalArgumentException("Matrix dimensions mismatch: " + a.rows + "x" + a.cols + " * " + b.rows + "x" + b.cols);
        }
        Matrix result = new Matrix(a.rows, b.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < b.cols; j++) {
                double sum = 0;
                for (int k = 0; k < a.cols; k++) {
                    sum += a.data[i][k] * b.data[k][j];
                }
                result.data[i][j] = sum;
            }
        }
        return result;
    }

    public static Matrix add(Matrix a, Matrix b) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException("Matrix dimensions mismatch for add");
        }
        Matrix result = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = a.data[i][j] + b.data[i][j];
            }
        }
        return result;
    }

    public static Matrix subtract(Matrix a, Matrix b) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException("Matrix dimensions mismatch for subtract");
        }
        Matrix result = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = a.data[i][j] - b.data[i][j];
            }
        }
        return result;
    }

    public static Matrix transpose(Matrix a) {
        Matrix result = new Matrix(a.cols, a.rows);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[j][i] = a.data[i][j];
            }
        }
        return result;
    }

    public static Matrix scalarMultiply(Matrix a, double s) {
        Matrix result = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = a.data[i][j] * s;
            }
        }
        return result;
    }

    public static Matrix hadamard(Matrix a, Matrix b) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException("Matrix dimensions mismatch for hadamard");
        }
        Matrix result = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = a.data[i][j] * b.data[i][j];
            }
        }
        return result;
    }

    public static Matrix applyFunction(Matrix a, Function<Double, Double> f) {
        Matrix result = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = f.apply(a.data[i][j]);
            }
        }
        return result;
    }

    public static Matrix randomInit(int rows, int cols, double min, double max) {
        Random rand = new Random();
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = min + (max - min) * rand.nextDouble();
            }
        }
        return result;
    }

    public static Matrix fromArray(double[] arr) {
        Matrix result = new Matrix(arr.length, 1);
        for (int i = 0; i < arr.length; i++) {
            result.data[i][0] = arr[i];
        }
        return result;
    }

    public static double[] toArray(Matrix m) {
        double[] result = new double[m.rows];
        for (int i = 0; i < m.rows; i++) {
            result[i] = m.data[i][0];
        }
        return result;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"rows\":").append(rows).append(",\"cols\":").append(cols).append(",\"data\":[");
        for (int i = 0; i < rows; i++) {
            sb.append("[");
            for (int j = 0; j < cols; j++) {
                sb.append(data[i][j]);
                if (j < cols - 1) sb.append(",");
            }
            sb.append("]");
            if (i < rows - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static Matrix fromJson(String json) {
        json = json.trim();
        int rowsStart = json.indexOf("\"rows\":") + 7;
        int rowsEnd = json.indexOf(",", rowsStart);
        int rows = Integer.parseInt(json.substring(rowsStart, rowsEnd).trim());

        int colsStart = json.indexOf("\"cols\":") + 7;
        int colsEnd = json.indexOf(",", colsStart);
        int cols = Integer.parseInt(json.substring(colsStart, colsEnd).trim());

        Matrix result = new Matrix(rows, cols);
        int dataStart = json.indexOf("\"data\":[") + 8;
        int dataEnd = json.lastIndexOf("]}");
        String dataStr = json.substring(dataStart, dataEnd);

        int r = 0, c = 0;
        boolean inArray = false;
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < dataStr.length(); i++) {
            char ch = dataStr.charAt(i);
            if (ch == '[') {
                inArray = true;
                c = 0;
                num.setLength(0);
            } else if (ch == ']') {
                if (num.length() > 0 && r < rows && c < cols) {
                    result.data[r][c] = Double.parseDouble(num.toString().trim());
                }
                num.setLength(0);
                inArray = false;
                r++;
            } else if (ch == ',' && inArray) {
                if (num.length() > 0 && r < rows && c < cols) {
                    result.data[r][c] = Double.parseDouble(num.toString().trim());
                    c++;
                }
                num.setLength(0);
            } else if (inArray) {
                num.append(ch);
            }
        }
        return result;
    }
}
