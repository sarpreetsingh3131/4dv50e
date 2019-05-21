package smc.runmodes;

import mapek.AdaptationOption;
import mapek.Goal;
import mapek.Goals;
import org.json.JSONArray;
import org.json.JSONObject;
import util.ConfigLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Comparison extends SMCConnector {

    @Override
    public void startVerification() {
        activforms();
        if (cycles <= TRAINING_CYCLE) {
            training();
        } else {
            testing();
        }
    }

    void activforms() {
        for (AdaptationOption adaptationOption : adaptationOptions) {
            smcChecker.checkCAO(
                    adaptationOption.toModelString(),
                    environment.toModelString(),
                    adaptationOption.verificationResults
            );
            adaptationOption.isVerified = true;
        }
    }

    void training() {
        sendToLearner(adaptationOptions, Mode.TRAINING);
        saveComparison(new JSONArray());
    }

    void testing() {
        JSONObject response = sendToLearner(adaptationOptions, Mode.TESTING);
        JSONArray learnerSelectedOptionsIndexes = response.getJSONArray("indexes");

        List<AdaptationOption> verifiedOptions = new LinkedList<>();

        for(int i = 0; i < learnerSelectedOptionsIndexes.length(); i++) {
            verifiedOptions.add(adaptationOptions.get(learnerSelectedOptionsIndexes.getInt(i)));
        }

        sendToLearner(verifiedOptions, Mode.TRAINING);
        saveComparison(learnerSelectedOptionsIndexes);
    }

    void saveComparison(JSONArray learnerSelectedOptionsIndexes) {
        JSONObject data = parseData(learnerSelectedOptionsIndexes);

        try {
            FileWriter writer = new FileWriter(
                    Paths.get(
                            System.getProperty("user.dir"),
                            "activforms", "log", "comparison_cycle_" + cycles + ".json")
                            .toString()
            );

            writer.write(data.toString(2));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    JSONObject parseData(JSONArray learnerSelectedOptionsIndexes) {
        JSONArray packetLossTarget = new JSONArray();
        JSONArray latencyTarget = new JSONArray();
        JSONArray energyConsumption = new JSONArray();

        for (AdaptationOption adaptationOption : adaptationOptions) {
            packetLossTarget.put(adaptationOption.verificationResults.packetLoss);
            latencyTarget.put(adaptationOption.verificationResults.latency);
            energyConsumption.put(adaptationOption.verificationResults.energyConsumption);
        }

        JSONObject data = new JSONObject();
        data.put("packet_loss", packetLossTarget);
        data.put("latency", latencyTarget);
        data.put("energy_consumption", energyConsumption);
        data.put("indexes_of_selected_adaptation_options", learnerSelectedOptionsIndexes);
        return data;
    }

    /*
    @Override
    public void startVerification() {
        boolean training = cycles <= TRAINING_CYCLE;
        switch (taskType) {
            case CLASSIFICATION:
            case REGRESSION:
                //singlePlGoal(training);
                break;
            case PLLAMULTICLASS:
            case PLLAMULTIREGR:
                //doublePlLaGoals(training);
                break;
            default:
                throw new RuntimeException(
                        String.format("Unsupported task type for Comparison: %s", taskType.val));
        }
    }

    private void singlePlGoal(boolean training) {
        JSONObject adjInspection = new JSONObject();
        adjInspection.put("adapIndices", new JSONArray());
        adjInspection.put("packetLoss", new JSONArray());
        adjInspection.put("energyConsumption", new JSONArray());
        adjInspection.put("latency", new JSONArray());
        adjInspection.put("regressionPLBefore", new JSONArray());
        adjInspection.put("classificationBefore", new JSONArray());
        adjInspection.put("regressionPLAfter", new JSONArray());
        adjInspection.put("classificationAfter", new JSONArray());

        if (cycles == 1) {
            // At the first cycle, no regression or classification output can be retrieved yet
            // -> use -1 as dummy prediction values
            IntStream.range(0, adaptationOptions.size()).forEach(i -> {
                adjInspection.getJSONArray("regressionPLBefore").put(-1);
                adjInspection.getJSONArray("classificationBefore").put(-1);
            });
        } else {
            // If not at the first cycle, retrieve the results predicted before online learning
            predictionLearners1Goal(adaptationOptions, adjInspection.getJSONArray("classificationBefore"),
                    adjInspection.getJSONArray("regressionPLBefore"));
        }


        // Check all the adaptation options with activFORMS
        int timeCap = ConfigLoader.getInstance().getTimeCap();
        List<Long> verifTimes = new ArrayList<>();

        for (AdaptationOption adaptationOption : adaptationOptions) {
            long startTime = System.currentTimeMillis();
            smcChecker.checkCAO(adaptationOption.toModelString(), environment.toModelString(),
                    adaptationOption.verificationResults);
            adaptationOption.isVerified = true;
            verifTimes.add(System.currentTimeMillis() - startTime);

            adjInspection.getJSONArray("packetLoss").put(adaptationOption.verificationResults.packetLoss);
            adjInspection.getJSONArray("energyConsumption").put(adaptationOption.verificationResults.energyConsumption);
            adjInspection.getJSONArray("latency").put(adaptationOption.verificationResults.latency);
            adjInspection.getJSONArray("adapIndices").put(adaptationOption.overallIndex);
        }


        if (training) {
            // If we are training, send the entire adaptation space (or limited space due to time constraints) to the learners,
            // and check what they have learned afterwards
            int amtOptions = adaptationOptions.size();
            long totalTime = 0;
            List<Integer> verifiedOptions = new ArrayList<>();

            for (int i = 0; i < amtOptions; i++) {
                int actualIndex = (i + lastLearningIndex) % amtOptions;

                if (totalTime / 1000 > timeCap) {
                    lastLearningIndex = actualIndex;
                    break;
                }

                totalTime += verifTimes.get(actualIndex);
                verifiedOptions.add(actualIndex);
            }

            // Retrieve the actual adaptation options which have been verified by using the indices
            List<AdaptationOption> options = verifiedOptions.stream().map(i -> adaptationOptions.get(i)).collect(Collectors.toList());

            System.out.print(";" + options.size());
            System.out.print(";" + adaptationOptions.size());

            send(options, TaskType.CLASSIFICATION, Mode.TRAINING);
            send(options, TaskType.REGRESSION, Mode.TRAINING);

            predictionLearners1Goal(adaptationOptions, adjInspection.getJSONArray("classificationAfter"),
                    adjInspection.getJSONArray("regressionPLAfter"));

        } else {

            // If we are testing, send the adjustments to the learning models and check their predictions again

            // Parse the classification and regression results from the JSON responses.
            final List<Integer> classificationResults = adjInspection.getJSONArray("classificationBefore")
                    .toList().stream().map(o -> Integer.parseInt(o.toString()))
                    .collect(Collectors.toList());
            final List<Float> regressionResults = adjInspection.getJSONArray("regressionPLBefore")
                    .toList().stream().map(o -> Float.parseFloat(o.toString()))
                    .collect(Collectors.toList());

            Goal pl = goals.getPacketLossGoal();

            List<Integer> overallIndicesClass = new ArrayList<>();
            List<Integer> overallIndicesRegr = new ArrayList<>();

            // Determine which adaptation options have to be sent back for the specific learners
            for (int i = 0; i < adaptationOptions.size(); i++) {
                if (classificationResults.get(i).equals(1)) {
                    overallIndicesClass.add(i);
                }
                if (pl.evaluate(regressionResults.get(i))) {
                    overallIndicesRegr.add(i);
                }
            }

            // In case the adaptation space of a prediction is 0, consider all adaptation options for online learning
            if (overallIndicesClass.isEmpty()) {
                for (int i = 0; i < adaptationOptions.size(); i++) {
                    overallIndicesClass.add(i);
                }
            }
            if (overallIndicesRegr.isEmpty()) {
                for (int i = 0; i < adaptationOptions.size(); i++) {
                    overallIndicesRegr.add(i);
                }
            }

            Collections.shuffle(overallIndicesClass);
            Collections.shuffle(overallIndicesRegr);

            int combinedTime = 0;
            for (int i = 0; i < overallIndicesClass.size(); i++) {
                int actualIndex = overallIndicesClass.get(i);
                if (combinedTime / 1000 > timeCap) {
                    overallIndicesClass = overallIndicesClass.subList(0, i);
                    break;
                }
                combinedTime += verifTimes.get(actualIndex);
            }

            combinedTime = 0;
            for (int i = 0; i < overallIndicesRegr.size(); i++) {
                int actualIndex = overallIndicesRegr.get(i);
                if (combinedTime / 1000 > timeCap) {
                    overallIndicesRegr = overallIndicesRegr.subList(0, i);
                    break;
                }
                combinedTime += verifTimes.get(actualIndex);
            }

            List<AdaptationOption> learningOptionsClass =
                    overallIndicesClass.stream().map(i -> adaptationOptions.get(i)).collect(Collectors.toList());
            List<AdaptationOption> learningOptionsRegr =
                    overallIndicesRegr.stream().map(i -> adaptationOptions.get(i)).collect(Collectors.toList());

            System.out.print(";" + learningOptionsClass.size());
            System.out.print(";" + learningOptionsRegr.size());
            System.out.print(";" + adaptationOptions.size());

            // Send the adaptation options specific to the learners back for online learning
            send(learningOptionsClass, TaskType.CLASSIFICATION, Mode.TRAINING);
            send(learningOptionsRegr, TaskType.REGRESSION, Mode.TRAINING);

            // Test the predictions of the learners again after online learning to track their adjustments
            predictionLearners1Goal(adaptationOptions, adjInspection.getJSONArray("classificationAfter"),
                    adjInspection.getJSONArray("regressionPLAfter"));
        }


        // Send the overall results to be saved on the server
        send(adjInspection, TaskType.NONE, Mode.COMPARISON);
    }

    private void addPredictionsBeforeLearning(JSONArray cl, JSONArray rePL, JSONArray reLA) {
        if (cycles == 1) {
            // At the first cycle, no regression or classification output can be retrieved yet
            // -> use -1 as dummy prediction values
            IntStream.range(0, adaptationOptions.size()).forEach(i -> {
                cl.put(-1);
                rePL.put(-1);
                reLA.put(-1);
            });
        } else {
            // If not at the first cycle, retrieve the results predicted before online learning
            predictionLearners2Goals(adaptationOptions, cl, rePL, reLA);
        }
    }

    private void doublePlLaGoals(boolean training) {
        JSONObject adjInspection = new JSONObject();
        adjInspection.put("adapIndices", new JSONArray());
        adjInspection.put("packetLoss", new JSONArray());
        adjInspection.put("energyConsumption", new JSONArray());
        adjInspection.put("latency", new JSONArray());
        adjInspection.put("regressionPLBefore", new JSONArray());
        adjInspection.put("regressionLABefore", new JSONArray());
        adjInspection.put("classificationBefore", new JSONArray());
        adjInspection.put("regressionPLAfter", new JSONArray());
        adjInspection.put("regressionLAAfter", new JSONArray());
        adjInspection.put("classificationAfter", new JSONArray());

        addPredictionsBeforeLearning(adjInspection.getJSONArray("classificationBefore"),
                adjInspection.getJSONArray("regressionPLBefore"),
                adjInspection.getJSONArray("regressionLABefore"));


        List<Long> verifTimes = new ArrayList<>();
        // Check all the adaptation options with activFORMS
        for (AdaptationOption adaptationOption : adaptationOptions) {
            Long startTime = System.currentTimeMillis();
            smcChecker.checkCAO(adaptationOption.toModelString(), environment.toModelString(),
                    adaptationOption.verificationResults);
            adaptationOption.isVerified = true;
            verifTimes.add(System.currentTimeMillis() - startTime);

            adjInspection.getJSONArray("packetLoss").put(adaptationOption.verificationResults.packetLoss);
            adjInspection.getJSONArray("energyConsumption").put(adaptationOption.verificationResults.energyConsumption);
            adjInspection.getJSONArray("latency").put(adaptationOption.verificationResults.latency);
            adjInspection.getJSONArray("adapIndices").put(adaptationOption.overallIndex);
        }

        int timeCap = ConfigLoader.getInstance().getTimeCap();

        if (training) {
            int amtOptions = adaptationOptions.size();
            long totalTime = 0;
            List<Integer> verifiedOptions = new ArrayList<>();

            for (int i = 0; i < amtOptions; i++) {
                int actualIndex = (i + lastLearningIndex) % amtOptions;

                if (totalTime / 1000 > timeCap) {
                    lastLearningIndex = actualIndex;
                    break;
                }

                totalTime += verifTimes.get(actualIndex);
                verifiedOptions.add(actualIndex);
            }
            List<AdaptationOption> options = verifiedOptions.stream().map(i -> adaptationOptions.get(i)).collect(Collectors.toList());
            System.out.print(";" + options.size());
            System.out.print(";" + adaptationOptions.size());

            // If we are training, send the entire adaptation space to the learners and check what they have learned
            send(options, TaskType.PLLAMULTICLASS, Mode.TRAINING);
            send(options, TaskType.PLLAMULTIREGR, Mode.TRAINING);

            predictionLearners2Goals(adaptationOptions, adjInspection.getJSONArray("classificationAfter"),
                    adjInspection.getJSONArray("regressionPLAfter"), adjInspection.getJSONArray("regressionLAAfter"));

        } else {
            // If we are testing, send the adjustments to the learning models and check their predictions again
            // Parse the classification and regression results from the JSON responses.
            final List<Integer> classificationResults = adjInspection.getJSONArray("classificationBefore")
                    .toList().stream().map(o -> Integer.parseInt(o.toString()))
                    .collect(Collectors.toList());
            final List<Float> regressionResultsPL = adjInspection.getJSONArray("regressionPLBefore")
                    .toList().stream().map(o -> Float.parseFloat(o.toString()))
                    .collect(Collectors.toList());
            final List<Float> regressionResultsLA = adjInspection.getJSONArray("regressionLABefore")
                    .toList().stream().map(o -> Float.parseFloat(o.toString()))
                    .collect(Collectors.toList());

            Goal pl = Goals.getInstance().getPacketLossGoal();
            Goal la = Goals.getInstance().getLatencyGoal();

            // Convert the regression predictions to the classes used in classification
            List<Integer> regressionResults = new ArrayList<>();
            for (int i = 0; i < regressionResultsPL.size(); i++) {
                regressionResults.add(
                        (pl.evaluate(regressionResultsPL.get(i)) ? 1 : 0) +
                                (la.evaluate(regressionResultsLA.get(i)) ? 2 : 0)
                );
            }

            List<Integer> classificationIndices = getOnlineLearningIndices(classificationResults, verifTimes);
            List<Integer> regressionIndices = getOnlineLearningIndices(regressionResults, verifTimes);


            List<AdaptationOption> learningOptionsClass =
                    classificationIndices.stream().map(i -> adaptationOptions.get(i)).collect(Collectors.toList());
            List<AdaptationOption> learningOptionsRegr =
                    regressionIndices.stream().map(i -> adaptationOptions.get(i)).collect(Collectors.toList());

            System.out.print(";" + learningOptionsClass.size());
            System.out.print(";" + learningOptionsRegr.size());
            System.out.print(";" + adaptationOptions.size());

            // Send the adaptation options specific to the learners back for online learning
            send(learningOptionsClass, TaskType.PLLAMULTICLASS, Mode.TRAINING);
            send(learningOptionsRegr, TaskType.PLLAMULTIREGR, Mode.TRAINING);

            // Test the predictions of the learners again after online learning to track their adjustments
            predictionLearners2Goals(adaptationOptions, adjInspection.getJSONArray("classificationAfter"),
                    adjInspection.getJSONArray("regressionPLAfter"), adjInspection.getJSONArray("regressionLAAfter"));
        }


        // Send the overall results to be saved on the server
        send(adjInspection, TaskType.NONE, Mode.COMPARISON);
    }

    private List<Integer> getOnlineLearningIndices(List<Integer> predictedClasses, List<Long> verificationTimes) {

        int predictionsInClass[] = new int[4];
        for (Integer pred : predictedClasses) {
            predictionsInClass[pred] += 1;
        }

        // The indices for the options of the best class predicted
        List<Integer> indicesMain = new ArrayList<>();
        // The indices for the options which are considered for exploration
        List<Integer> indicesSub = new ArrayList<>();

        if (predictionsInClass[3] > 0) {
            // There is at least one option which satisfies both goals
            for (int i = 0; i < predictedClasses.size(); i++) {
                int prediction = predictedClasses.get(i);
                if (prediction == 3) {
                    indicesMain.add(i);
                } else if (prediction == 2 || prediction == 1) {
                    indicesSub.add(i);
                }
            }
        } else if (predictionsInClass[2] + predictionsInClass[1] > 0) {
            // There is at least one option which satisfies one of the goals
            for (int i = 0; i < predictedClasses.size(); i++) {
                int prediction = predictedClasses.get(i);
                if (prediction == 0) {
                    indicesSub.add(i);
                } else {
                    indicesMain.add(i);
                }
            }
        } else {
            for (int i = 0; i < predictedClasses.size(); i++) {
                indicesMain.add(i);
            }
        }

        double explorationPercentage = ConfigLoader.getInstance().getExplorationPercentage();

        // Shuffle the main indices first (to ensure all options are reached after some time in case not all can be verified each cycle)
        Collections.shuffle(indicesMain);
        // Similar reasoning for the exploration indices
        Collections.shuffle(indicesSub);

        // Only select a percentage of the predictions of the other classes
        int subIndex = (int) Math.floor(indicesSub.size() * explorationPercentage);
        indicesSub = indicesSub.subList(0, subIndex);

        List<Integer> overallIndices = new ArrayList<>();
        overallIndices.addAll(indicesMain);
        overallIndices.addAll(indicesSub);


        int timeCap = ConfigLoader.getInstance().getTimeCap();
        int lastIndex = overallIndices.size();
        int totalTime = 0;

        for (int i = 0; i < overallIndices.size(); i++) {
            if (totalTime / 1000 > timeCap) {
                lastIndex = i;
                break;
            }

            totalTime += verificationTimes.get(i);
        }

        return overallIndices.subList(0, lastIndex);
    }
    */

    /**
     * Helper function which adds the predictions of both learning models to their respective JSON arrays.
     * The predictions are made over the whole adaptation space.
     *
     * @param classArray The JSON array which will hold the classification predictions.
     * @param regrArray  The JSON array which will hold the regression predictions.
     */

    /*
    private void predictionLearners1Goal(List<AdaptationOption> adaptationOptions, JSONArray classArray, JSONArray regrArray) {
        JSONObject classificationResponse = send(adaptationOptions, TaskType.CLASSIFICATION, Mode.TESTING);
        JSONObject regressionResponse = send(adaptationOptions, TaskType.REGRESSION, Mode.TESTING);

        for (Object item : classificationResponse.getJSONArray("predictions")) {
            classArray.put(Integer.parseInt(item.toString()));
        }
        for (Object item : regressionResponse.getJSONArray("predictions")) {
            regrArray.put(Float.parseFloat(item.toString()));
        }
    }
    */

    /**
     * Helper function which adds the predictions of both learning models to their respective JSON arrays.
     * The predictions are made over the whole adaptation space.
     *
     * @param classArray  The JSON array which will hold the classification predictions.
     * @param regrArrayPL The JSON array which will hold the regression predictions for packet loss.
     * @param regrArrayLA The JSON array which will hold the regression predictions for latency.
     */

    /*
    private void predictionLearners2Goals(List<AdaptationOption> adaptationOptions, JSONArray classArray,
                                          JSONArray regrArrayPL, JSONArray regrArrayLA) {

        JSONObject classificationResponse = send(adaptationOptions, TaskType.PLLAMULTICLASS, Mode.TESTING);
        JSONObject regressionResponse = send(adaptationOptions, TaskType.PLLAMULTIREGR, Mode.TESTING);

        for (Object item : classificationResponse.getJSONArray("predictions")) {
            classArray.put(Integer.parseInt(item.toString()));
        }

        JSONArray pred_pl = regressionResponse.getJSONArray("predictions_pl");
        JSONArray pred_la = regressionResponse.getJSONArray("predictions_la");

        for (int i = 0; i < pred_pl.length(); i++) {
            regrArrayPL.put(Float.parseFloat(pred_pl.get(i).toString()));
            regrArrayLA.put(Float.parseFloat(pred_la.get(i).toString()));
        }
    }
    */
}
