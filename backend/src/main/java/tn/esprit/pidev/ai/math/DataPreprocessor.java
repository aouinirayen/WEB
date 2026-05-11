package tn.esprit.pidev.ai.math;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPreprocessor {

    private double[] mins;
    private double[] maxs;
    private int featureCount;
    private boolean fitted = false;

    public DataPreprocessor() {}

    public DataPreprocessor(int featureCount) {
        this.featureCount = featureCount;
        this.mins = new double[featureCount];
        this.maxs = new double[featureCount];
    }

    public double normalize(double value, double min, double max) {
        if (max - min == 0) return 0.5;
        return (value - min) / (max - min);
    }

    public double denormalize(double normalized, double min, double max) {
        return normalized * (max - min) + min;
    }

    public double[][] fitTransform(double[][] data) {
        if (data.length == 0) return data;
        featureCount = data[0].length;
        mins = new double[featureCount];
        maxs = new double[featureCount];

        for (int j = 0; j < featureCount; j++) {
            mins[j] = Double.MAX_VALUE;
            maxs[j] = -Double.MAX_VALUE;
        }
        for (double[] row : data) {
            for (int j = 0; j < featureCount; j++) {
                if (row[j] < mins[j]) mins[j] = row[j];
                if (row[j] > maxs[j]) maxs[j] = row[j];
            }
        }
        fitted = true;

        double[][] normalized = new double[data.length][featureCount];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < featureCount; j++) {
                normalized[i][j] = normalize(data[i][j], mins[j], maxs[j]);
            }
        }
        return normalized;
    }

    public double[] transform(double[] input) {
        if (!fitted) throw new IllegalStateException("DataPreprocessor not fitted yet");
        double[] normalized = new double[input.length];
        for (int j = 0; j < input.length; j++) {
            if (j < featureCount) {
                normalized[j] = normalize(input[j], mins[j], maxs[j]);
            } else {
                normalized[j] = input[j];
            }
        }
        return normalized;
    }

    public void save(String filePath) throws IOException {
        Path dir = Paths.get(filePath).getParent();
        if (dir != null && !Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"featureCount\":").append(featureCount).append(",\"mins\":[");
        for (int i = 0; i < featureCount; i++) {
            sb.append(mins[i]);
            if (i < featureCount - 1) sb.append(",");
        }
        sb.append("],\"maxs\":[");
        for (int i = 0; i < featureCount; i++) {
            sb.append(maxs[i]);
            if (i < featureCount - 1) sb.append(",");
        }
        sb.append("]}");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        }
    }

    public void load(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        json = json.trim();

        int fcStart = json.indexOf("\"featureCount\":") + 15;
        int fcEnd = json.indexOf(",", fcStart);
        featureCount = Integer.parseInt(json.substring(fcStart, fcEnd).trim());

        mins = parseJsonArray(json, "\"mins\":");
        maxs = parseJsonArray(json, "\"maxs\":");
        fitted = true;
    }

    private double[] parseJsonArray(String json, String key) {
        int start = json.indexOf(key) + key.length();
        int arrStart = json.indexOf("[", start) + 1;
        int arrEnd = json.indexOf("]", arrStart);
        String arrStr = json.substring(arrStart, arrEnd);
        String[] parts = arrStr.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }
        return result;
    }

    public double[] getMins() { return mins; }
    public double[] getMaxs() { return maxs; }
    public int getFeatureCount() { return featureCount; }
    public boolean isFitted() { return fitted; }
}
