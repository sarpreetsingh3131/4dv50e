package domain;

import java.util.HashMap;

public final class DomainConstants {
    public static final double coulomb = 1000.0;
    public static final double receptionCost = 14.2;
    public static final int receptionTime = 2;
    private static final double[] powerConsumptionRate = {20.2, 21.2, 22.3, 23.7, 24.7, 26.1, 27.5, 28.8, 30.0, 31.2,
            32.4, 33.7, 35.1, 36.5, 38.0, 38.9};
    private static final HashMap<Integer, Double> sfTimes = createSfTimes();
    // Prevent creation of this class
    // It's just a container for some global knowledge, given by the guy who created
    // the real-life DeltaIoT system
    private DomainConstants() {
    }

    private static HashMap<Integer, Double> createSfTimes() {
        HashMap<Integer, Double> sfTimes = new HashMap<>();
        sfTimes.put(7, 0.128);
        sfTimes.put(8, 0.258);
        sfTimes.put(9, 0.458);
        sfTimes.put(10, 0.858);
        sfTimes.put(11, 1.158);
        sfTimes.put(12, 1.58);
        return sfTimes;
    }

    public static double getPowerConsumptionRate(int number) {
        return powerConsumptionRate[number];
    }

    public static double getSfTime(int sfTime) {
        return sfTimes.get(sfTime);
    }

    public static double getPacketDuplicationSfTime() {
        return sfTimes.get(8);
    }

    public static double getPacketDuplicationConsumptionRate() {
        return powerConsumptionRate[15];
    }
}
