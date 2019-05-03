package deltaiot.services;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;

public class Mote {

    @Expose
    private int MoteID;
    @Expose
    private int Load;
    @Expose
    private double BatteryRemaining;
    @Expose
    private double BatteryConsumed;
    @Expose
    private int NumParents;
    @Expose
    private int DataProbability;
    @Expose
    private List<Link> Links = new LinkedList<Link>();
    @Expose
    private int KeepAliveTime;
    // @Expose private double FilledQueue;
    @Expose
    private int QueueLoss;
    @Expose
    private int QueueSize;
    @Expose
    private int MAX_QUEUE_SIZE;

    public Mote() {
    }

    public Mote(Integer moteid, int load, Double battery, Integer parents, int dataProbability, int queueSize,
                int queueLoss, int keepAliveTime, int MAX_QUEUE_SIZE, List<Link> links) {
        this.MoteID = moteid;
        this.Load = load;
        this.BatteryRemaining = battery;
        this.NumParents = parents;
        this.DataProbability = dataProbability;
        this.QueueLoss = queueLoss;
        this.QueueSize = queueSize;
        this.MAX_QUEUE_SIZE = MAX_QUEUE_SIZE;
        this.KeepAliveTime = keepAliveTime;
        this.Links = links;
    }

    public Integer getMoteid() {
        return MoteID;
    }

    public void setMoteid(Integer moteid) {
        this.MoteID = moteid;
    }

    /**
     * @return The load
     */
    public int getLoad() {
        return Load;
    }

    /**
     * @param load The load
     */
    public void setLoad(int load) {
        this.Load = load;
    }

    /**
     * @return The statistics
     */
    public List<Link> getLinks() {
        return Links;
        // return null;
    }

    /**
     * @param statistics The statistics
     */
    public void setLinks(List<Link> links) {
        this.Links = links;
    }

    /**
     * @return The battery
     */
    public double getBattery() {
        return BatteryRemaining;
    }

    /**
     * @param battery The battery
     */
    public void setBattery(double battery) {
        this.BatteryRemaining = battery;
    }

    /**
     * @return The parents
     */
    public int getParents() {
        return NumParents;
    }

    /**
     * @param parents The parents
     */
    public void setParents(int parents) {
        this.NumParents = parents;
    }

    public int getDataProbability() {
        return DataProbability;
    }

    public void setDataProbability(int dataProbability) {
        DataProbability = dataProbability;
    }

    public Link getLink(int index) {
        return Links.get(index);
    }

    public Link getLinkWithDest(int destination) {
        for (Link link : Links)
            if (link.getDest() == destination)
                return link;

        return null;
    }

    public void addLink(Link link) {
        Links.add(link);
        NumParents++;
    }

    public int getKeepAliveTime() {
        return KeepAliveTime;
    }

    public int getQueueLoss() {
        return QueueLoss;
    }

    public int getMaxQeueueSize() {
        return MAX_QUEUE_SIZE;
    }

    public int getCurrentQSize() {
        return QueueSize;
    }

    public void setCurrentQSize(int queueSize) {
        QueueSize = queueSize;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("MoteId=%d, Parents=%d, Battery=%f, Load=%d, DataProbability=%d", MoteID, NumParents,
                BatteryRemaining, Load, DataProbability));
        for (Link link : Links)
            str.append(String.format("[%s]", link.toString()));

        return str.toString();
    }

    @Override
    public Object clone() {
        List<Link> linksCopy = new LinkedList<Link>();

        for (Link link : Links)
            linksCopy.add((Link) link.clone());
        return new Mote(MoteID, Load, BatteryRemaining, NumParents, DataProbability, QueueSize, QueueLoss,
                KeepAliveTime, MAX_QUEUE_SIZE, linksCopy);
    }
}
