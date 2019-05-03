package domain;

public class RunInfo {

    private Profile<Double> globalInterference = new Constant<>(0.0);
    private int runNumber = 0;

    public RunInfo() {

    }

    public Profile<Double> getGlobalInterference() {
        return globalInterference;
    }

    public void setGlobalInterference(Profile<Double> globalInterference) {
        this.globalInterference = globalInterference;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }

    public void incrementRunNumber() {
        runNumber += 1;
    }
}
