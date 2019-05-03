package deltaiot.services;

public class QoS {
    private double PacketLoss;
    private double PowerConsumption;
    private double Latency;
    private double QueueLoss;
    private double Sent;

    private String period;

    public double getPacketLoss() {
        return PacketLoss;
    }

    public void setPacketLoss(double packetLoss) {
        this.PacketLoss = packetLoss;
    }

    public double getEnergyConsumption() {
        return PowerConsumption;
    }

    public void setEnergyConsumption(double energyConsumption) {
        this.PowerConsumption = energyConsumption;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return String.format("Period:%s, PacketLoss:%f, EnergyConsumption:%f, Latency:%f, QueueLoss:%f", period,
                PacketLoss, PowerConsumption, Latency, QueueLoss);
    }

    public double getLatency() {
        return Latency;
    }

    public void setLatency(double latency) {
        Latency = latency;
    }

    public double getQueueLoss() {
        return QueueLoss;
    }

    public void setQueueLoss(double queueLoss) {
        QueueLoss = queueLoss;
    }

    public double getSent() {
        return Sent;
    }

    public void setSent(double sent) {
        Sent = sent;
    }
}
