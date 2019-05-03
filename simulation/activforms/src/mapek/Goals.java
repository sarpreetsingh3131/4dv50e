package mapek;

import util.ConfigLoader;

import java.util.List;
import java.util.Optional;

public class Goals {

    private static Goals instance = null;
    private List<Goal> goals;

    private Goals() {
        goals = ConfigLoader.getInstance().getGoals();
    }

    public static Goals getInstance() {
        if (instance == null) {
            instance = new Goals();
        }
        return instance;
    }


    public Goal getPacketLossGoal() {
        return getGoal("packetLoss");
    }

    public Goal getLatencyGoal() {
        return getGoal("latency");
    }

    private Goal getGoal(String quality) {
        Optional<Goal> goal = goals.stream().filter(g -> g.getTarget().equals(quality)).findFirst();

        if (goal.isPresent()) {
            return goal.get();
        }
        throw new RuntimeException(String.format("There is no %s goal present.", quality));
    }

    public boolean optimizeGoalEnergyConsumption(AdaptationOption bestAdaptationOption, AdaptationOption option) {
        if (bestAdaptationOption == null)
            return true;
        return option.verificationResults.energyConsumption < bestAdaptationOption.verificationResults.energyConsumption;
    }

}
