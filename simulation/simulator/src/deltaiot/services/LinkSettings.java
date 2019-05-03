package deltaiot.services;

public class LinkSettings {
    private int src;
    private int dest;
    private int powerSettings = -1;
    private int distributionFactor = -1;
    private int spreadingFactor;

    public LinkSettings() {

    }

    public LinkSettings(int src, int dest, int powerSettings, int distributionFactor, int spreadingFactor) {
        this.src = src;
        this.dest = dest;
        this.powerSettings = powerSettings;
        this.distributionFactor = distributionFactor;
        this.spreadingFactor = spreadingFactor;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public int getDest() {
        return dest;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public int getPowerSettings() {
        return powerSettings;
    }

    public void setPowerSettings(int powerSettings) {
        this.powerSettings = powerSettings;
    }

    public int getDistributionFactor() {
        return distributionFactor;
    }

    public void setDistributionFactor(int distributionFactor) {
        this.distributionFactor = distributionFactor;
    }

    public int getSpreadingFactor() {
        return spreadingFactor;
    }

    public void setSpreadingFactor(int spreadingFactor) {
        this.spreadingFactor = spreadingFactor;
    }

    @Override
    public String toString() {
        return String.format("src=%d,dest=%d,powerSettings=%d,distributionFactor=%d,spreadingFactor=%d", src, dest,
                powerSettings, distributionFactor, spreadingFactor);
    }
}
