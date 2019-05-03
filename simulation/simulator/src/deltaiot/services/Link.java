package deltaiot.services;

import com.google.gson.annotations.Expose;

public class Link {

    private double Latency;
    private int SF = 8;

    @Expose
    private int Power;
    @Expose
    private int PacketLoss;
    @Expose
    private int Source;
    @Expose
    private int Destination;
    @Expose
    private double SNR;
    @Expose
    private int Distribution;

    public Link() {
    }

    public Link(double latency, int power, int packetLoss, int source, int dest, double sNR, int distribution, int sF) {
        Latency = latency;
        Power = power;
        PacketLoss = packetLoss;
        this.Source = source;
        this.Destination = dest;
        SNR = sNR;
        Distribution = distribution;
        SF = sF;
    }

    public Integer getSource() {
        return Source;
    }

    public void setSource(Integer source) {
        this.Source = source;
    }

    /**
     * @return The latency
     */
    public double getLatency() {
        return Latency;
    }

    /**
     * @param latency The Latency
     */
    public void setLatency(double latency) {
        this.Latency = latency;
    }

    /**
     * @return The power
     */
    public int getPower() {
        return Power;
    }

    /**
     * @param power The Power
     */
    public void setPower(int power) {
        this.Power = power;
    }

    /**
     * @return The packetLoss
     */
    public int getPacketLoss() {
        return PacketLoss;
    }

    /**
     * @param packetLoss The PacketLoss
     */
    public void setPacketLoss(int packetLoss) {
        this.PacketLoss = packetLoss;
    }

    /**
     * @return The dest
     */
    public Integer getDest() {
        return Destination;
    }

    /**
     * @param dest The dest
     */
    public void setDest(Integer dest) {
        this.Destination = dest;
    }

    /**
     * @return The sNR
     */
    public Double getSNR() {
        return SNR;
    }

    /**
     * @param sNR The SNR
     */
    public void setSNR(Double sNR) {
        this.SNR = sNR;
    }

    /**
     * @return The distribution
     */
    public int getDistribution() {
        return Distribution;
    }

    /**
     * @param distribution The Distribution
     */
    public void setDistribution(int distribution) {
        this.Distribution = distribution;
    }

    /**
     * @return The sF
     */
    public int getSF() {
        return SF;
    }

    /**
     * @param sF The SF
     */
    public void setSF(int sF) {
        this.SF = sF;
    }

    @Override
    public String toString() {
        return String.format(
                "Latency: %f, Power: %d, PacketLoss: %d, dest: %d, source: %d, SNR: %f, Distribution: %d, SF: %d",
                Latency, Power, PacketLoss, Destination, Source, SNR, Distribution, SF);
    }

    @Override
    protected Object clone() {
        return new Link(Latency, Power, PacketLoss, Source, Destination, SNR, Distribution, SF);
    }
}
