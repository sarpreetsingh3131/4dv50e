package domain;

public class Link {

    public static int sentPackets = 0;
    public static int lostPackets = 0;
    private Node from;
    private Node to;
    private double latency;
    private int powerNumber;
    private int sfTimeNumber = 8; // corresponds to some time to send, see settings
    private Gateway direction;
    private int distribution;
    private double SNR;
    private SNREquation snrEquation = new SNREquation(0, 0);
    private Profile<Double> interference = new Constant<>(0.0);

    // Constructor
    Link(Node from, Node to, Gateway direction, int powerNumber, int distribution) {
        this.from = from;
        this.to = to;
        this.direction = direction;
        this.powerNumber = powerNumber;
        this.distribution = distribution;
    }

    // Functionality
    void sendPacket(Packet packet, RunInfo runInfo) {
        int packetLoss = calculatePacketLoss(runInfo);

        // System.out.println(this + ": packetLoss" + packetLoss);
        if (Math.random() * 100 + 1 > packetLoss) {
            sentPackets++;
            to.receivePacket(packet, runInfo);
        } else {
//			 /System.out.println("packet is lost");
            lostPackets++;
            direction.reportPacketLost(packet);
        }
    }

    public int calculatePacketLoss(RunInfo runInfo) {
        double snr = getSNR(runInfo);
        if (snr > 0) {
            return 0;
        } else if (snr < -20) {
            return 100;
        } else {
            return (int) (Math.abs(snr / 2) * 10);
        }
    }

    // Getters & setters
    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public double getLatency() {
        return latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public int getPowerNumber() {
        return powerNumber;
    }

    public void setPowerNumber(int power) {
        this.powerNumber = power;
    }

    public double getPowerConsumptionRate() {
        return DomainConstants.getPowerConsumptionRate(powerNumber);
    }

    public int getSfTimeNumber() {
        return sfTimeNumber;
    }

    public void setSfTimeNumber(int sfTime) {
        this.sfTimeNumber = sfTime;
    }

    public double getSfTime() {
        return DomainConstants.getSfTime(sfTimeNumber);
    }

    public Gateway getDirection() {
        return direction;
    }

    public void setDirection(Gateway direction) {
        this.direction = direction;
    }

    public int getDistribution() {
        return distribution;
    }

    public void setDistribution(int distribution) {
        this.distribution = distribution;
    }

    public SNREquation getSnrEquation() {
        return snrEquation;
    }

    public void setSnrEquation(SNREquation snrEquation) {
        this.snrEquation = snrEquation;
    }

    public Profile<Double> getInterference() {
        return interference;
    }

    public void setInterference(Profile<Double> interference) {
        this.interference = interference;
    }

    public double getSNR(RunInfo runInfo) { // TODO Perhaps better name 'calculateSNR'?
        // Calculate SNR based on the power
        double snr = snrEquation.getSNR(powerNumber);

        // Calculate interference as the max interference from several possible sources
        int runNumber = runInfo.getRunNumber();
        double linkInt = interference.get(runNumber);
        double fromInt = from.getInterference().get(runNumber);
        double toInt = to.getInterference().get(runNumber);
        double globalInt = runInfo.getGlobalInterference().get(runNumber);
        double interference = Math.max(Math.max(linkInt, fromInt), Math.max(toInt, globalInt));

        // Total snr
        this.SNR = snr - interference; // interference makes snr worse
        // System.out.format("%d --> %d:(%d)(%f)(%f) = %f\n", from.getId(), to.getId(),
        // powerNumber, snr, interference, SNR);
        return this.SNR;
    }

    /*
     * Return previously calculated SNR
     */
    public double getSNR() {
        return SNR;
    }

    @Override
    public String toString() {
        return "Link [" + from.getId() + "->" + to.getId()
                // + ", latency=" + latency
                + ", power=" + powerNumber
                // + ", sfTimeNumber=" + sfTimeNumber
                + ", distribution=" + distribution + "]";
    }
}
