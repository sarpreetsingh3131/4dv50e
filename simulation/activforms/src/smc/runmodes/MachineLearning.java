package smc.runmodes;

import mapek.AdaptationOption;
import mapek.Goal;
import mapek.Goals;
import org.json.JSONArray;
import org.json.JSONObject;
import util.ConfigLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MachineLearning extends SMCConnector {

    @Override
    public void startVerification() {
        initializeTimer();

        if (cycles <= TRAINING_CYCLE) {
            training();
        } else {
            testing();
        }

        destructTimer();
    }

    void training() {
        List<AdaptationOption> verifiedOptions = new LinkedList<>();

        long startTime = System.currentTimeMillis();
        for (AdaptationOption adaptationOption : adaptationOptions) {
            if (overTime) {
                break;
            }

            smcChecker.checkCAO(
                    adaptationOption.toModelString(),
                    environment.toModelString(),
                    adaptationOption.verificationResults
            );
            adaptationOption.isVerified = true;
            verifiedOptions.add(adaptationOption);
        }
        long endTime = System.currentTimeMillis() - startTime;

        System.out.print(";" + verifiedOptions.size() + ";" + endTime);

        sendToLearner(verifiedOptions, Mode.TRAINING);
    }

    void testing() {
        JSONObject response = sendToLearner(adaptationOptions, Mode.TESTING);
        JSONArray validOptionsIndexes = response.getJSONArray("indexes");
        System.out.print(";" + validOptionsIndexes.length());

        List<AdaptationOption> verifiedOptions = new LinkedList<>();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < validOptionsIndexes.length(); i++) {
            if (overTime) {
                break;
            }
            int index = validOptionsIndexes.getInt(i);
            AdaptationOption adaptationOption = adaptationOptions.get(index);
            smcChecker.checkCAO(
                    adaptationOption.toModelString(),
                    environment.toModelString(),
                    adaptationOption.verificationResults
            );
            adaptationOption.isVerified = true;
            verifiedOptions.add(adaptationOption);
        }
        long endTime = System.currentTimeMillis() - startTime;

        System.out.print(";" + verifiedOptions.size() + ";" + endTime);

        sendToLearner(verifiedOptions, Mode.TRAINING);
    }



    /*
    private void training() {
        // Formally verify all the adaptation options, and send them to the learners for training
        int amtOptions = adaptationOptions.size();

        AdaptationOption adaptationOption;
        for (int i = 0; i < amtOptions; i++) {
            int actualIndex = (i + lastLearningIndex) % amtOptions;

            if (overTime) {
                lastLearningIndex = actualIndex;
                break;
            }

            adaptationOption = adaptationOptions.get(actualIndex);
            smcChecker.checkCAO(adaptationOption.toModelString(), environment.toModelString(),
                    adaptationOption.verificationResults);
            adaptationOption.isVerified = true;
        }

        //Send all features + target, add function to get trainig time
        send(adaptationOptions.stream()
                        .filter(o -> o.isVerified)
                        .collect(Collectors.toList()),
                taskType,
                Mode.TRAINING
        );
    }


    private void testing() {
        if (taskType == TaskType.CLASSIFICATION || taskType == TaskType.REGRESSION) {
            testing1Goal();
        } else if (taskType == TaskType.PLLAMULTICLASS || taskType == TaskType.PLLAMULTIREGR) {
            testing2Goals();
        } else {
            throw new RuntimeException(String.format("Testing unsupported for mode: %s", taskType.val));
        }
    }

    private void testing1Goal() {
        // Send the adaptation options to the learner with mode testing, returns the predictions of the learner
        JSONObject response = send(adaptationOptions, taskType, Mode.TESTING);

        // Retrieve the amount of options that were predicted to meet the goal by the learner
        int adaptationSpace = Integer.parseInt(response.get("adaptation_space").toString());

        ArrayList<Float> predictions = new ArrayList<>();
        JSONArray arr = response.getJSONArray("predictions");

        for (int i = 0; i < arr.length(); i++) {
            predictions.add(Float.parseFloat(arr.get(i).toString()));
        }

        // No exploration for single goal verification
        List<Integer> overallIndices = new ArrayList<>();
        Goal pl = goals.getPacketLossGoal();

        if (adaptationSpace != 0) {
            for (int i = 0; i < adaptationOptions.size(); i++) {
                boolean prediction = taskType == TaskType.CLASSIFICATION ?
                        predictions.get(i) == 1.0 :
                        pl.evaluate(predictions.get(i));

                if (prediction) {
                    overallIndices.add(i);
                }
            }
        } else {
            for (int i = 0; i < adaptationOptions.size(); i++) {
                overallIndices.add(i);
            }
        }

        // Fair distribution of options in case not all of them can be verified
        Collections.shuffle(overallIndices);

        AdaptationOption option;

        for (Integer index : overallIndices) {
            if (overTime) {
                break;
            }
            option = adaptationOptions.get(index);
            smcChecker.checkCAO(option.toModelString(), environment.toModelString(),
                    option.verificationResults);
            option.isVerified = true;
        }

        List<AdaptationOption> learningOptions =
                adaptationOptions.stream().filter(o -> o.isVerified).collect(Collectors.toList());

        System.out.print(";" + learningOptions.size());

        // Perform online learning on the samples that were predicted to meet the user goal
        send(learningOptions, taskType, Mode.TRAINING);
    }


    private void testing2Goals() {
        // Send the adaptation options to the learner with mode testing, returns the predictions of the learner
        JSONObject response = send(adaptationOptions, taskType, Mode.TESTING);

        // The different prediction classes in case of 2 goals (latency & packet loss)
        int[] amtPredClass = {0, 0, 0, 0};
        ArrayList<Integer> predictions = new ArrayList<>();

        switch (taskType) {
            case PLLAMULTICLASS:
                JSONArray pred = response.getJSONArray("predictions");

                for (int i = 0; i < pred.length(); i++) {
                    int predictedClass = Integer.parseInt(pred.get(i).toString());
                    amtPredClass[predictedClass]++;
                    predictions.add(predictedClass);
                }
                break;

            case PLLAMULTIREGR:
                JSONArray pred_pl = response.getJSONArray("predictions_pl");
                JSONArray pred_la = response.getJSONArray("predictions_la");

                Goal pl = Goals.getInstance().getPacketLossGoal();
                Goal la = Goals.getInstance().getLatencyGoal();

                for (int i = 0; i < pred_la.length(); i++) {
                    int predictedClass =
                            (pl.evaluate(Double.parseDouble(pred_pl.get(i).toString())) ? 1 : 0) +
                                    (la.evaluate(Double.parseDouble(pred_la.get(i).toString())) ? 2 : 0);
                    amtPredClass[predictedClass]++;
                    predictions.add(predictedClass);
                }
                break;

            default:
                throw new RuntimeException(
                        String.format("Trying to do testing for 2 goals with incompatible task type '%s'.", taskType.val));
        }

        // The indices for the options of the best class predicted
        List<Integer> indicesMain = new ArrayList<>();
        // The indices for the options which are considered for exploration
        List<Integer> indicesSub = new ArrayList<>();

        if (amtPredClass[3] > 0) {
            // There is at least one option which satisfies both goals
            for (int i = 0; i < predictions.size(); i++) {
                int prediction = predictions.get(i);
                if (prediction == 3) {
                    indicesMain.add(i);
                } else if (prediction == 2 || prediction == 1) {
                    indicesSub.add(i);
                }
            }
        } else if (amtPredClass[2] + amtPredClass[1] > 0) {
            // There is at least one option which satisfies one of the goals
            for (int i = 0; i < predictions.size(); i++) {
                int prediction = predictions.get(i);
                if (prediction == 0) {
                    indicesSub.add(i);
                } else {
                    indicesMain.add(i);
                }
            }
        } else {
            for (int i = 0; i < predictions.size(); i++) {
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


        AdaptationOption adaptationOption;

        for (Integer index : overallIndices) {
            if (overTime) {
                break;
            }

            adaptationOption = adaptationOptions.get(index);
            smcChecker.checkCAO(adaptationOption.toModelString(), environment.toModelString(),
                    adaptationOption.verificationResults);
            adaptationOption.isVerified = true;
        }

        List<AdaptationOption> learningOptions =
                adaptationOptions.stream().filter(o -> o.isVerified).collect(Collectors.toList());

        System.out.print(";" + learningOptions.size());
        // Perform online learning on the samples that were predicted to meet the user goal
        send(learningOptions, taskType, Mode.TRAINING);
    }
    */
}
