package mapek;

public class SNREquation {
    int source;
    int destination;
    double multiplier;
    double constant;

    public SNREquation(int source, int destination, double multiplier, double constant) {
        this.source = source;
        this.destination = destination;
        this.multiplier = multiplier;
        this.constant = constant;
    }
}
