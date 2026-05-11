package tn.esprit.pidev.ai.model;

import tn.esprit.pidev.ai.math.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NeuralNetwork {

    private static final Logger log = LoggerFactory.getLogger(NeuralNetwork.class);

    private int[] layerSizes;
    private Matrix[] weights;
    private Matrix[] biases;
    private double learningRate;
    private double trainingLoss;
    private double accuracy;
    private double testLoss;
    private double testAccuracy;

    public NeuralNetwork(int... layerSizes) {
        this.layerSizes = layerSizes;
        this.weights = new Matrix[layerSizes.length - 1];
        this.biases = new Matrix[layerSizes.length - 1];
        initializeWeights();
    }

    private NeuralNetwork() {}

    private void initializeWeights() {
        for (int i = 0; i < weights.length; i++) {
            double limit = Math.sqrt(2.0 / layerSizes[i]);
            weights[i] = Matrix.randomInit(layerSizes[i + 1], layerSizes[i], -limit, limit);
            biases[i] = new Matrix(layerSizes[i + 1], 1);
        }
    }

    public double[] forward(double[] input) {
        Matrix activation = Matrix.fromArray(input);
        for (int i = 0; i < weights.length; i++) {
            Matrix z = Matrix.add(Matrix.multiply(weights[i], activation), biases[i]);
            if (i == weights.length - 1) {
                activation = Matrix.applyFunction(z, this::sigmoid);
            } else {
                activation = Matrix.applyFunction(z, this::relu);
            }
        }
        return Matrix.toArray(activation);
    }

    public void train(double[][] inputs, double[][] targets, int epochs, double lr) {
        this.learningRate = lr;
        int n = inputs.length;

        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalLoss = 0;
            int correct = 0;

            for (int s = 0; s < n; s++) {
                Matrix[] activations = new Matrix[layerSizes.length];
                Matrix[] zValues = new Matrix[layerSizes.length - 1];

                activations[0] = Matrix.fromArray(inputs[s]);
                for (int i = 0; i < weights.length; i++) {
                    zValues[i] = Matrix.add(Matrix.multiply(weights[i], activations[i]), biases[i]);
                    if (i == weights.length - 1) {
                        activations[i + 1] = Matrix.applyFunction(zValues[i], this::sigmoid);
                    } else {
                        activations[i + 1] = Matrix.applyFunction(zValues[i], this::relu);
                    }
                }

                double[] output = Matrix.toArray(activations[activations.length - 1]);
                for (int j = 0; j < targets[s].length; j++) {
                    double pred = Math.max(1e-8, Math.min(1 - 1e-8, output[j]));
                    totalLoss += -(targets[s][j] * Math.log(pred) + (1 - targets[s][j]) * Math.log(1 - pred));
                }

                if ((output[0] >= 0.5 && targets[s][0] == 1.0) || (output[0] < 0.5 && targets[s][0] == 0.0)) {
                    correct++;
                }

                // dL/dz for output layer (sigmoid + BCE) = (a - y)
                Matrix dz = Matrix.subtract(activations[activations.length - 1], Matrix.fromArray(targets[s]));
                for (int i = weights.length - 1; i >= 0; i--) {
                    Matrix gradient;
                    if (i == weights.length - 1) {
                        // Output layer: dL/dz = (a - y) already correct
                        gradient = dz;
                    } else {
                        // Hidden layer: dL/dz = dL/da * relu'(z)
                        gradient = Matrix.hadamard(dz, Matrix.applyFunction(zValues[i], x -> reluDeriv(x)));
                    }
                    gradient = clipMatrix(gradient, 1.0);
                    Matrix scaledGrad = Matrix.scalarMultiply(gradient, learningRate);

                    Matrix delta = Matrix.multiply(scaledGrad, Matrix.transpose(activations[i]));
                    weights[i] = Matrix.subtract(weights[i], delta);
                    biases[i] = Matrix.subtract(biases[i], scaledGrad);

                    if (i > 0) {
                        // Propagate gradient to previous layer: dL/da_{i} = W^T * dL/dz_{i}
                        dz = Matrix.multiply(Matrix.transpose(weights[i]), gradient);
                    }
                }
            }

            this.trainingLoss = totalLoss / n;
            this.accuracy = (double) correct / n;

            if (epoch % 100 == 0 || epoch == epochs - 1) {
                log.info("NeuralNetwork Epoch {} | Loss: {} | Accuracy: {}",
                        epoch, String.format("%.6f", trainingLoss), String.format("%.4f", accuracy));
            }
        }
    }

    public double[] predict(double[] input) {
        return forward(input);
    }

    public void evaluate(double[][] testInputs, double[][] testTargets) {
        int n = testInputs.length;
        if (n == 0) return;
        double totalLoss = 0;
        int correct = 0;
        for (int s = 0; s < n; s++) {
            double[] output = forward(testInputs[s]);
            double pred = Math.max(1e-8, Math.min(1 - 1e-8, output[0]));
            totalLoss += -(testTargets[s][0] * Math.log(pred) + (1 - testTargets[s][0]) * Math.log(1 - pred));
            if ((output[0] >= 0.5 && testTargets[s][0] == 1.0) || (output[0] < 0.5 && testTargets[s][0] == 0.0)) {
                correct++;
            }
        }
        this.testLoss = totalLoss / n;
        this.testAccuracy = (double) correct / n;
        log.info("NeuralNetwork TEST Evaluation — Loss: {} | Accuracy: {}",
                String.format("%.4f", testLoss), String.format("%.4f", testAccuracy));
    }

    public double getTestLoss() { return testLoss; }
    public double getTestAccuracy() { return testAccuracy; }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-Math.max(-500, Math.min(500, x))));
    }

    private double sigmoidDeriv(double x) {
        double s = sigmoid(x);
        return s * (1 - s);
    }

    private double relu(double x) {
        return x > 0 ? x : 0.01 * x;
    }

    private double reluDeriv(double x) {
        return x > 0 ? 1.0 : 0.01;
    }

    private Matrix clipMatrix(Matrix m, double maxVal) {
        return Matrix.applyFunction(m, x -> Math.max(-maxVal, Math.min(maxVal, x)));
    }

    public void saveModel(String filePath) throws IOException {
        Path dir = Paths.get(filePath).getParent();
        if (dir != null && !Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"layerSizes\":[");
        for (int i = 0; i < layerSizes.length; i++) {
            sb.append(layerSizes[i]);
            if (i < layerSizes.length - 1) sb.append(",");
        }
        sb.append("],\"trainingLoss\":").append(trainingLoss);
        sb.append(",\"accuracy\":").append(accuracy);
        sb.append(",\"weights\":[");
        for (int i = 0; i < weights.length; i++) {
            sb.append(weights[i].toJson());
            if (i < weights.length - 1) sb.append(",");
        }
        sb.append("],\"biases\":[");
        for (int i = 0; i < biases.length; i++) {
            sb.append(biases[i].toJson());
            if (i < biases.length - 1) sb.append(",");
        }
        sb.append("]}");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        }
    }

    public static NeuralNetwork loadModel(String filePath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        NeuralNetwork nn = new NeuralNetwork();

        String lsStr = json.substring(json.indexOf("\"layerSizes\":[") + 14);
        lsStr = lsStr.substring(0, lsStr.indexOf("]"));
        String[] lsParts = lsStr.split(",");
        nn.layerSizes = new int[lsParts.length];
        for (int i = 0; i < lsParts.length; i++) {
            nn.layerSizes[i] = Integer.parseInt(lsParts[i].trim());
        }

        int tlStart = json.indexOf("\"trainingLoss\":") + 15;
        int tlEnd = json.indexOf(",", tlStart);
        nn.trainingLoss = Double.parseDouble(json.substring(tlStart, tlEnd).trim());

        int accStart = json.indexOf("\"accuracy\":") + 11;
        int accEnd = json.indexOf(",", accStart);
        nn.accuracy = Double.parseDouble(json.substring(accStart, accEnd).trim());

        nn.weights = new Matrix[nn.layerSizes.length - 1];
        nn.biases = new Matrix[nn.layerSizes.length - 1];

        int weightsStart = json.indexOf("\"weights\":[") + 11;
        int biasesKeyStart = json.indexOf("],\"biases\":[");
        String weightsStr = json.substring(weightsStart, biasesKeyStart);

        int biasesStart = biasesKeyStart + 12;
        int biasesEnd = json.lastIndexOf("]}");
        String biasesStr = json.substring(biasesStart, biasesEnd);

        parseMatrixArray(weightsStr, nn.weights);
        parseMatrixArray(biasesStr, nn.biases);

        return nn;
    }

    private static void parseMatrixArray(String str, Matrix[] target) {
        int depth = 0;
        int start = -1;
        int idx = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0 && idx < target.length) {
                    target[idx] = Matrix.fromJson(str.substring(start, i + 1));
                    idx++;
                    start = -1;
                }
            }
        }
    }

    public double getTrainingLoss() { return trainingLoss; }
    public double getAccuracy() { return accuracy; }
    public int[] getLayerSizes() { return layerSizes; }
}
