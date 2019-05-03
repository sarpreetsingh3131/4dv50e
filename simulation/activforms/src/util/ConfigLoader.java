package util;

import mapek.Goal;
import smc.runmodes.SMCConnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Class Used to load the properties listed in the SMCConfig.properties file.
 */
public class ConfigLoader {

    public static final String configFileLocation = Paths.get(
            System.getProperty("user.dir"),
            "SMCConfig.properties"
    ).toString();

    private static ConfigLoader instance = null;
    private Properties properties;

    private ConfigLoader() {
        // Only load the properties file once (singleton pattern)
        properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(configFileLocation);
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Could not load the properties file correctly at location %f.", configFileLocation.toString())
            );
        }
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    public String getProperty(String key) {
        String property = properties.getProperty(key);
        if (property != null) {
            return property.trim();
        } else {
            throw new RuntimeException(
                    String.format("Property '%f' not found in the properties file. Make sure this property is provided.", key)
            );
        }
    }

    public int getAmountOfLearningCycles() {
        return Integer.parseInt(this.getProperty("amountOfLearningCycles"));
    }

    public int getAmountOfCycles() {
        return Integer.parseInt(this.getProperty("amountOfCycles"));
    }

    public int getDistributionGap() {
        return Integer.parseInt(this.getProperty("distributionGap"));
    }

    public int getTimeCap() {
        return Integer.parseInt(this.getProperty("cappedVerificationTime"));
    }

    public SMCConnector.Mode getRunMode() {
        return SMCConnector.Mode.getMode(this.getProperty("runMode").toLowerCase());
    }

    public String getSimulationNetwork() {
        return this.getProperty("simulationNetwork");
    }

    public boolean getVerificationTimeConstraint() {
        return Boolean.valueOf(this.getProperty("verificationTimeConstraint"));
    }

    public int getLearnerPort() {
        return Integer.valueOf(this.getProperty("learnerPort"));
    }

    public List<Goal> getGoals() {
        List<Goal> goals = new ArrayList<>();

        String targets[] = this.getProperty("targets").split(",");
        String thressholds[] = this.getProperty("thressholds").split(",");
        String operators[] = this.getProperty("operators").split(",");

        for (int i = 0; i < targets.length; i++) {
            goals.add(new Goal(
                    targets[i].trim(), operators[i].trim(), Double.parseDouble(thressholds[i].trim())));
        }
        return goals;
    }

    /*public SMCConnector.TaskType getTaskType() {
        return SMCConnector.TaskType.getTaskType(this.getProperty("taskType").toLowerCase());
    }

    public double getExplorationPercentage() {
        return Double.parseDouble(this.getProperty("explorationPercentage"));
    }


    public boolean timeInReadableFormat() {
        return this.getProperty("readableTimeFormat").toLowerCase().equals("true");
    }

    public boolean shouldDeletePreviousModels() {
        return this.getProperty("deletePreviousModels").toLowerCase().equals("true");
    }
    */
}


