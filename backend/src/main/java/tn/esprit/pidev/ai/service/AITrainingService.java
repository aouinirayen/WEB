package tn.esprit.pidev.ai.service;

import tn.esprit.pidev.ai.data.DataGenerator;
import tn.esprit.pidev.ai.dto.AIStatsResponse;
import tn.esprit.pidev.ai.math.DataPreprocessor;
import tn.esprit.pidev.ai.model.LinearRegression;
import tn.esprit.pidev.ai.model.LogisticRegression;
import tn.esprit.pidev.ai.model.NeuralNetwork;
import tn.esprit.pidev.entity.AITrainingData;
import tn.esprit.pidev.repository.AITrainingDataRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AITrainingService {

    private static final Logger log = LoggerFactory.getLogger(AITrainingService.class);

    private static final String MODEL_DIR = "ai-models";
    private static final String MATCHING_MODEL_FILE = MODEL_DIR + "/matching_nn.json";
    private static final String MATCHING_PREPROCESSOR_FILE = MODEL_DIR + "/matching_preprocessor.json";
    private static final String CANCELLATION_MODEL_FILE = MODEL_DIR + "/cancellation_lr.json";
    private static final String CANCELLATION_PREPROCESSOR_FILE = MODEL_DIR + "/cancellation_preprocessor.json";
    private static final String SATISFACTION_MODEL_FILE = MODEL_DIR + "/satisfaction_linreg.json";
    private static final String SATISFACTION_PREPROCESSOR_FILE = MODEL_DIR + "/satisfaction_preprocessor.json";

    private final AITrainingDataRepository trainingDataRepository;
    private final MatchingService matchingService;
    private final CancellationPredictionService cancellationService;
    private final SatisfactionPredictionService satisfactionService;

    private NeuralNetwork matchingNN;
    private LogisticRegression cancellationLR;
    private LinearRegression satisfactionLinReg;
    private boolean modelsReady = false;

    public AITrainingService(AITrainingDataRepository trainingDataRepository,
                              MatchingService matchingService,
                              CancellationPredictionService cancellationService,
                              SatisfactionPredictionService satisfactionService) {
        this.trainingDataRepository = trainingDataRepository;
        this.matchingService = matchingService;
        this.cancellationService = cancellationService;
        this.satisfactionService = satisfactionService;
    }

    @PostConstruct
    public void init() {
        try {
            Path modelDir = Paths.get(MODEL_DIR);
            if (!Files.exists(modelDir)) {
                Files.createDirectories(modelDir);
            }

            if (modelsExist()) {
                log.info("=== AI Models found on disk. Loading... ===");
                loadAllModels();
                modelsReady = true;
                log.info("=== All 3 AI models loaded successfully ===");
            } else {
                log.info("=== No AI models found. Generating data and training... ===");
                generateAndTrain();
                log.info("=== All 3 AI models trained and saved ===");
            }
        } catch (Exception e) {
            log.error("AI initialization failed: {}", e.getMessage(), e);
        }
    }

    private boolean modelsExist() {
        return new File(MATCHING_MODEL_FILE).exists()
                && new File(CANCELLATION_MODEL_FILE).exists()
                && new File(SATISFACTION_MODEL_FILE).exists();
    }

    private void loadAllModels() throws Exception {
        matchingNN = NeuralNetwork.loadModel(MATCHING_MODEL_FILE);
        DataPreprocessor matchingPrep = new DataPreprocessor();
        matchingPrep.load(MATCHING_PREPROCESSOR_FILE);
        matchingService.setModel(matchingNN, matchingPrep);

        cancellationLR = LogisticRegression.loadModel(CANCELLATION_MODEL_FILE);
        DataPreprocessor cancellationPrep = new DataPreprocessor();
        cancellationPrep.load(CANCELLATION_PREPROCESSOR_FILE);
        cancellationService.setModel(cancellationLR, cancellationPrep);

        satisfactionLinReg = LinearRegression.loadModel(SATISFACTION_MODEL_FILE);
        DataPreprocessor satisfactionPrep = new DataPreprocessor();
        satisfactionPrep.load(SATISFACTION_PREPROCESSOR_FILE);
        satisfactionService.setModel(satisfactionLinReg, satisfactionPrep);
    }

    public void generateAndTrain() {
        try {
            DataGenerator generator = new DataGenerator();
            Random rand = new Random(42);

            // ==================== Module 1: Matching (Neural Network) ====================
            log.info("--- Training Module 1: Matching Neural Network ---");
            List<AITrainingData> matchingData = generator.generateMatchingData(5000);
            trainingDataRepository.saveAll(matchingData);

            double[][] matchFeatures = DataGenerator.parseFeatures(matchingData);
            double[][] matchLabels = DataGenerator.parseLabelsAsMatrix(matchingData);

            // Train/Test Split 80/20
            int matchSplit = (int) (matchFeatures.length * 0.8);
            int[] matchIdx = shuffleIndices(matchFeatures.length, rand);
            double[][] matchTrainF = pickRows(matchFeatures, matchIdx, 0, matchSplit);
            double[][] matchTrainL = pickRows(matchLabels, matchIdx, 0, matchSplit);
            double[][] matchTestF = pickRows(matchFeatures, matchIdx, matchSplit, matchFeatures.length);
            double[][] matchTestL = pickRows(matchLabels, matchIdx, matchSplit, matchFeatures.length);

            log.info("Matching split: {} train / {} test", matchTrainF.length, matchTestF.length);

            DataPreprocessor matchingPrep = new DataPreprocessor();
            double[][] matchTrainNorm = matchingPrep.fitTransform(matchTrainF);
            double[][] matchTestNorm = transformRows(matchingPrep, matchTestF);

            matchingNN = new NeuralNetwork(7, 32, 16, 1);
            matchingNN.train(matchTrainNorm, matchTrainL, 3000, 0.005);
            matchingNN.evaluate(matchTestNorm, matchTestL);

            matchingNN.saveModel(MATCHING_MODEL_FILE);
            matchingPrep.save(MATCHING_PREPROCESSOR_FILE);
            matchingService.setModel(matchingNN, matchingPrep);
            log.info("Matching NN — Train Accuracy: {} | TEST Accuracy: {}",
                    String.format("%.4f", matchingNN.getAccuracy()),
                    String.format("%.4f", matchingNN.getTestAccuracy()));

            // ==================== Module 2: Cancellation (Logistic Regression) ====================
            log.info("--- Training Module 2: Cancellation Logistic Regression ---");
            List<AITrainingData> cancellationData = generator.generateCancellationData(3000);
            trainingDataRepository.saveAll(cancellationData);

            double[][] cancelFeatures = DataGenerator.parseFeatures(cancellationData);
            double[] cancelLabels = DataGenerator.parseLabels(cancellationData);

            // Train/Test Split 80/20
            int cancelSplit = (int) (cancelFeatures.length * 0.8);
            int[] cancelIdx = shuffleIndices(cancelFeatures.length, rand);
            double[][] cancelTrainF = pickRows(cancelFeatures, cancelIdx, 0, cancelSplit);
            double[] cancelTrainL = pickLabels(cancelLabels, cancelIdx, 0, cancelSplit);
            double[][] cancelTestF = pickRows(cancelFeatures, cancelIdx, cancelSplit, cancelFeatures.length);
            double[] cancelTestL = pickLabels(cancelLabels, cancelIdx, cancelSplit, cancelFeatures.length);

            log.info("Cancellation split: {} train / {} test", cancelTrainF.length, cancelTestF.length);

            DataPreprocessor cancellationPrep = new DataPreprocessor();
            double[][] cancelTrainNorm = cancellationPrep.fitTransform(cancelTrainF);
            double[][] cancelTestNorm = transformRows(cancellationPrep, cancelTestF);

            cancellationLR = new LogisticRegression(5);
            cancellationLR.train(cancelTrainNorm, cancelTrainL, 2000, 0.1);
            cancellationLR.evaluate(cancelTestNorm, cancelTestL);

            cancellationLR.saveModel(CANCELLATION_MODEL_FILE);
            cancellationPrep.save(CANCELLATION_PREPROCESSOR_FILE);
            cancellationService.setModel(cancellationLR, cancellationPrep);
            log.info("Cancellation LR — Train Accuracy: {} | TEST Accuracy: {}",
                    String.format("%.4f", cancellationLR.getAccuracy()),
                    String.format("%.4f", cancellationLR.getTestAccuracy()));

            // ==================== Module 3: Satisfaction (Linear Regression) ====================
            log.info("--- Training Module 3: Satisfaction Linear Regression ---");
            List<AITrainingData> satisfactionData = generator.generateSatisfactionData(5000);
            trainingDataRepository.saveAll(satisfactionData);

            double[][] satFeatures = DataGenerator.parseFeatures(satisfactionData);
            double[] satLabels = DataGenerator.parseLabels(satisfactionData);

            // Train/Test Split 80/20
            int satSplit = (int) (satFeatures.length * 0.8);
            int[] satIdx = shuffleIndices(satFeatures.length, rand);
            double[][] satTrainF = pickRows(satFeatures, satIdx, 0, satSplit);
            double[] satTrainL = pickLabels(satLabels, satIdx, 0, satSplit);
            double[][] satTestF = pickRows(satFeatures, satIdx, satSplit, satFeatures.length);
            double[] satTestL = pickLabels(satLabels, satIdx, satSplit, satFeatures.length);

            log.info("Satisfaction split: {} train / {} test", satTrainF.length, satTestF.length);

            DataPreprocessor satisfactionPrep = new DataPreprocessor();
            double[][] satTrainNorm = satisfactionPrep.fitTransform(satTrainF);
            double[][] satTestNorm = transformRows(satisfactionPrep, satTestF);

            satisfactionLinReg = new LinearRegression(5);
            satisfactionLinReg.train(satTrainNorm, satTrainL, 5000, 0.05);
            satisfactionLinReg.evaluate(satTestNorm, satTestL);

            satisfactionLinReg.saveModel(SATISFACTION_MODEL_FILE);
            satisfactionPrep.save(SATISFACTION_PREPROCESSOR_FILE);
            satisfactionService.setModel(satisfactionLinReg, satisfactionPrep);
            log.info("Satisfaction LinReg — Train R2: {} | TEST R2: {}",
                    String.format("%.4f", satisfactionLinReg.getR2Score()),
                    String.format("%.4f", satisfactionLinReg.getTestR2Score()));

            modelsReady = true;
        } catch (Exception e) {
            log.error("Training failed: {}", e.getMessage(), e);
            throw new RuntimeException("AI training failed", e);
        }
    }

    // ==================== Train/Test Split Utilities ====================

    private int[] shuffleIndices(int length, Random rand) {
        int[] indices = new int[length];
        for (int i = 0; i < length; i++) indices[i] = i;
        for (int i = length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }
        return indices;
    }

    private double[][] pickRows(double[][] data, int[] indices, int from, int to) {
        double[][] result = new double[to - from][];
        for (int i = from; i < to; i++) {
            result[i - from] = data[indices[i]];
        }
        return result;
    }

    private double[] pickLabels(double[] labels, int[] indices, int from, int to) {
        double[] result = new double[to - from];
        for (int i = from; i < to; i++) {
            result[i - from] = labels[indices[i]];
        }
        return result;
    }

    private double[][] transformRows(DataPreprocessor prep, double[][] data) {
        double[][] result = new double[data.length][];
        for (int i = 0; i < data.length; i++) {
            result[i] = prep.transform(data[i]);
        }
        return result;
    }

    public void retrainModels() {
        log.info("=== Retraining all AI models... ===");
        modelsReady = false;

        try {
            // Delete old model files
            new File(MATCHING_MODEL_FILE).delete();
            new File(MATCHING_PREPROCESSOR_FILE).delete();
            new File(CANCELLATION_MODEL_FILE).delete();
            new File(CANCELLATION_PREPROCESSOR_FILE).delete();
            new File(SATISFACTION_MODEL_FILE).delete();
            new File(SATISFACTION_PREPROCESSOR_FILE).delete();
        } catch (Exception e) {
            log.warn("Could not delete old model files: {}", e.getMessage());
        }

        generateAndTrain();
        log.info("=== Retraining complete ===");
    }

    public AIStatsResponse getStats() {
        AIStatsResponse stats = new AIStatsResponse();

        if (matchingNN != null) {
            Map<String, Object> matchStats = new HashMap<>();
            matchStats.put("type", "Neural Network MLP");
            matchStats.put("architecture", formatArchitecture(matchingNN.getLayerSizes()));
            matchStats.put("loss", matchingNN.getTrainingLoss());
            matchStats.put("accuracy", matchingNN.getAccuracy());
            matchStats.put("testLoss", matchingNN.getTestLoss());
            matchStats.put("testAccuracy", matchingNN.getTestAccuracy());
            matchStats.put("status", "READY");
            stats.setMatchingModel(matchStats);
        } else {
            Map<String, Object> matchStats = new HashMap<>();
            matchStats.put("status", "NOT_TRAINED");
            stats.setMatchingModel(matchStats);
        }

        if (cancellationLR != null) {
            Map<String, Object> cancelStats = new HashMap<>();
            cancelStats.put("type", "Logistic Regression");
            cancelStats.put("features", 5);
            cancelStats.put("loss", cancellationLR.getTrainingLoss());
            cancelStats.put("accuracy", cancellationLR.getAccuracy());
            cancelStats.put("testLoss", cancellationLR.getTestLoss());
            cancelStats.put("testAccuracy", cancellationLR.getTestAccuracy());
            cancelStats.put("status", "READY");
            stats.setCancellationModel(cancelStats);
        } else {
            Map<String, Object> cancelStats = new HashMap<>();
            cancelStats.put("status", "NOT_TRAINED");
            stats.setCancellationModel(cancelStats);
        }

        if (satisfactionLinReg != null) {
            Map<String, Object> satStats = new HashMap<>();
            satStats.put("type", "Linear Regression");
            satStats.put("features", 5);
            satStats.put("mse", satisfactionLinReg.getTrainingLoss());
            satStats.put("r2Score", satisfactionLinReg.getR2Score());
            satStats.put("testMSE", satisfactionLinReg.getTestMSE());
            satStats.put("testR2Score", satisfactionLinReg.getTestR2Score());
            satStats.put("status", "READY");
            stats.setSatisfactionModel(satStats);
        } else {
            Map<String, Object> satStats = new HashMap<>();
            satStats.put("status", "NOT_TRAINED");
            stats.setSatisfactionModel(satStats);
        }

        stats.setTotalTrainingData(trainingDataRepository.count());
        stats.setStatus(modelsReady ? "ALL_MODELS_READY" : "MODELS_NOT_READY");
        return stats;
    }

    private String formatArchitecture(int[] sizes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sizes.length; i++) {
            sb.append(sizes[i]);
            if (i < sizes.length - 1) sb.append(" -> ");
        }
        return sb.toString();
    }

    public boolean isReady() {
        return modelsReady;
    }
}
