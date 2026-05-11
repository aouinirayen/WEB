package tn.esprit.pidev.ai.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class LogisticRegression {

    private static final Logger log = LoggerFactory.getLogger(LogisticRegression.class);

    private double[] weights;
    private double bias;
    private double trainingLoss;
    private double accuracy;
    private double testLoss;
    private double testAccuracy;
    private int featureCount;

    public LogisticRegression(int featureCount) {
        this.featureCount = featureCount;
        this.weights = new double[featureCount];
        Random rand = new Random();
        for (int i = 0; i < featureCount; i++) {
            weights[i] = (rand.nextDouble() - 0.5) * 0.02;
        }
        this.bias = 0;
    }

    private LogisticRegression() {}

    public void train(double[][] X, double[] y, int epochs, double lr) {
        int n = X.length;

        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalLoss = 0;
            int correct = 0;

            for (int i = 0; i < n; i++) {
                double z = dotProduct(weights, X[i]) + bias;
                double prediction = sigmoid(z);

                double pred = Math.max(1e-8, Math.min(1 - 1e-8, prediction));
                totalLoss += -(y[i] * Math.log(pred) + (1 - y[i]) * Math.log(1 - pred));

                if ((prediction >= 0.5 && y[i] == 1.0) || (prediction < 0.5 && y[i] == 0.0)) {
                    correct++;
                }

                double error = prediction - y[i];
                for (int j = 0; j < weights.length; j++) {
                    weights[j] -= lr * error * X[i][j];
                }
                bias -= lr * error;
            }

            this.trainingLoss = totalLoss / n;
            this.accuracy = (double) correct / n;

            if (epoch % 100 == 0 || epoch == epochs - 1) {
                log.info("LogisticRegression Epoch {} | Loss: {} | Accuracy: {}", epoch, trainingLoss, accuracy);
            }
        }
    }

    public double predict(double[] x) {
        double z = dotProduct(weights, x) + bias;
        return sigmoid(z);
    }

    public void evaluate(double[][] testX, double[] testY) {
        int n = testX.length;
        if (n == 0) return;
        double totalLoss = 0;
        int correct = 0;
        for (int i = 0; i < n; i++) {
            double prediction = predict(testX[i]);
            double pred = Math.max(1e-8, Math.min(1 - 1e-8, prediction));
            totalLoss += -(testY[i] * Math.log(pred) + (1 - testY[i]) * Math.log(1 - pred));
            if ((prediction >= 0.5 && testY[i] == 1.0) || (prediction < 0.5 && testY[i] == 0.0)) {
                correct++;
            }
        }
        this.testLoss = totalLoss / n;
        this.testAccuracy = (double) correct / n;
        log.info("LogisticRegression TEST Evaluation \u2014 Loss: {} | Accuracy: {}",
                String.format("%.4f", testLoss), String.format("%.4f", testAccuracy));
    }

    public double getTestLoss() { return testLoss; }
    public double getTestAccuracy() { return testAccuracy; }

    public String getRiskLevel(double probability) {
        if (probability < 0.3) return "FAIBLE";
        if (probability < 0.6) return "MOYEN";
        return "ELEVE";
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-Math.max(-500, Math.min(500, x))));
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
        sb.append(",\"accuracy\":").append(accuracy);
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

    public static LogisticRegression loadModel(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        LogisticRegression model = new LogisticRegression();

        int fcStart = json.indexOf("\"featureCount\":") + 15;
        int fcEnd = json.indexOf(",", fcStart);
        model.featureCount = Integer.parseInt(json.substring(fcStart, fcEnd).trim());

        int biasStart = json.indexOf("\"bias\":") + 7;
        int biasEnd = json.indexOf(",", biasStart);
        model.bias = Double.parseDouble(json.substring(biasStart, biasEnd).trim());

        int tlStart = json.indexOf("\"trainingLoss\":") + 15;
        int tlEnd = json.indexOf(",", tlStart);
        model.trainingLoss = Double.parseDouble(json.substring(tlStart, tlEnd).trim());

        int accStart = json.indexOf("\"accuracy\":") + 11;
        int accEnd = json.indexOf(",", accStart);
        model.accuracy = Double.parseDouble(json.substring(accStart, accEnd).trim());

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
    public double getAccuracy() { return accuracy; }
    public double[] getWeights() { return weights; }
    public double getBias() { return bias; }
}
