package domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DoubleRange implements Profile<Double> {

    private double min;
    private double max;
    private Map<Integer, Double> memory = new HashMap<>();
    private Random rand = new Random(100);
    private int avg = 5;

    public DoubleRange(Double min, Double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Double get(int runNumber) {
        if (!memory.containsKey(runNumber)) {
            // return getAvg(runNumber);// memory.get(runNumber);
            // }
            // else {
            double random = min + rand.nextDouble() * (max - min);
            memory.put(runNumber, random);
            // return random;
        }

        return getAvg(runNumber);
    }

    private double getAvg(int runNumber) {
        double sum = 0;
        int count = 0;
        for (int i = runNumber; i >= 0 && i > runNumber - avg; i--) {
            sum += memory.get(i);
            count++;
        }
        return sum / count;

    }
}
