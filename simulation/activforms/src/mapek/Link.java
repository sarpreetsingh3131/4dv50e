package mapek;

public class Link {
    int source;
    int destination;
    int power;
    int distribution;

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getDistribution() {
        return distribution;
    }

    public void setDistribution(int distribution) {
        this.distribution = distribution;
    }

    public Link getCopy() {
        Link link = new Link();
        link.source = this.source;
        link.destination = this.destination;
        link.power = this.power;
        link.distribution = this.distribution;
        return link;
    }
}
