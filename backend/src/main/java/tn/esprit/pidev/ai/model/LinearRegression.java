package tn.esprit.pidev.ai.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class LinearRegression {

    private static final Logger log = LoggerFactory.getLogger(LinearRegression.class);

    private double[] weights;
    private double bias;
    private double trainingLoss;
    private double r2Score;
    private double testMSE;
    private double testR2Score;
    private int featureCount;

    public LinearRegression(int featureCount) {
        this.featureCount = featureCount;
        this.weights = new double[featureCount];
        Random rand = new Random();
        for (int i = 0; i < featureCount; i++) {
            weights[i] = (rand.nextDouble() - 0.5) * 0.02;
        }
        this.bias = 0;
    }

    private LinearRegression() {}

    public void train(double[][] X, double[] y, int epochs, double lr) {
        int n = X.length;

        double yMean = 0;
        for (double v : y) yMean += v;
        yMean /= n;

        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalError = 0;
            double ssRes = 0;
            double ssTot = 0;

            double[] gradW = new double[featureCount];
            double gradB = 0;

            for (int i = 0; i < n; i++) {
                double prediction = dotProduct(weights, X[i]) + bias;
                prediction = Math.max(1.0, Math.min(5.0, prediction));

                double error = prediction - y[i];
                totalError += error * error;
                ssRes += error * error;
                ssTot += (y[i] - yMean) * (y[i] - yMean);

                for (int j = 0; j < featureCount; j++) {
                    gradW[j] += 2.0 * error * X[i][j] / n;
                }
                gradB += 2.0 * error / n;
            }

            for (int j = 0; j < featureCount; j++) {
                weights[j] -= lr * gradW[j];
            }
            bias -= lr * gradB;

            this.trainingLoss = totalError / n;
            this.r2Score = (ssTot == 0) ? 1.0 : 1.0 - (ssRes / ssTot);

            if (epoch % 100 == 0 || epoch == epochs - 1) {
                log.info("LinearRegression Epoch {} | MSE: {} | R2: {}", epoch, trainingLoss, r2Score);
            }
        }
    }

    public double predict(double[] x) {
        double result = dotProduct(weights, x) + bias;
        return Math.max(1.0, Math.min(5.0, result));
    }

    public void evaluate(double[][] testX, double[] testY) {
        int n = testX.length;
        if (n == 0) return;
        double yMean = 0;
        for (double v : testY) yMean += v;
        yMean /= n;
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            double prediction = predict(testX[i]);
            double error = prediction - testY[i];
            ssRes += error * error;
            ssTot += (testY[i] - yMean) * (testY[i] - yMean);
        }
        this.testMSE = ssRes / n;
        this.testR2Score = (ssTot == 0) ? 1.0 : 1.0 - (ssRes / ssTot);
        log.info("LinearRegression TEST Evaluation \u2014 MSE: {} | R2: {}",
                String.format("%.4f", testMSE), String.format("%.4f", testR2Score));
    }

    public double getTestMSE() { return testMSE; }
    public double getTestR2Score() { return testR2Score; }

    public int getStarRating(double score) {
        return (int) Math.max(1, Math.min(5, Math.round(score)));
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public void saveModel(String filePath) throws IOException {
        Path dir = Paths.get(filePath).getParent();
        if (dir != null && !Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"featureCount\":").append(featureCount);
        sb.append(",\"bias\":").append(bias);
        sb.append(",\"trainingLoss\":").append(trainingLoss);
        sb.append(",\"r2Score\":").append(r2Score);
        sb.append(",\"weights\":[");
        for (int i = 0; i < weights.length; i++) {
            sb.append(weights[i]);
            if (i < weights.length - 1) sb.append(",");
        }
        sb.append("]}");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        }
    }

    public static LinearRegression loadModel(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        LinearRegression model = new LinearRegression();

        int fcStart = json.indexOf("\"featureCount\":") + 15;
        int fcEnd = json.indexOf(",", fcStart);
        model.featureCount = Integer.parseInt(json.substring(fcStart, fcEnd).trim());

        int biasStart = json.indexOf("\"bias\":") + 7;
        int biasEnd = json.indexOf(",", biasStart);
        model.bias = Double.parseDouble(json.substring(biasStart, biasEnd).trim());

        int tlStart = json.indexOf("\"trainingLoss\":") + 15;
        int tlEnd = json.indexOf(",", tlStart);
        model.trainingLoss = Double.parseDouble(json.substring(tlStart, tlEnd).trim());

        int r2Start = json.indexOf("\"r2Score\":") + 10;
        int r2End = json.indexOf(",", r2Start);
        model.r2Score = Double.parseDouble(json.substring(r2Start, r2End).trim());

        int wStart = json.indexOf("\"weights\":[") + 11;
        int wEnd = json.indexOf("]}", wStart);
        String[] wParts = json.substring(wStart, wEnd).split(",");
        model.weights = new double[wParts.length];
        for (int i = 0; i < wParts.length; i++) {
            model.weights[i] = Double.parseDouble(wParts[i].trim());
        }

        return model;
    }

    public double getTrainingLoss() { return trainingLoss; }
    public double getR2Score() { return r2Score; }
    public double[] getWeights() { return weights; }
    public double getBias() { return bias; }
}
