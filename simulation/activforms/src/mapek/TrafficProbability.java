package mapek;

/**
 * Class used to represent the probability that a mote sends traffic.
 * The load variable is a value between [0-100] denoting the probability.
 */
public class TrafficProbability {

    public int moteId;
    public double load;

    public TrafficProbability(int moteId, double load) {
        this.moteId = moteId;
        this.load = load;
    }

    public TrafficProbability getCopy() {
        return new TrafficProbability(moteId, load);
    }
}
