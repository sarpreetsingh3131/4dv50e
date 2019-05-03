package domain;

public class Packet {

    private static int identifier = 1;
    private Mote source;
    private int number;
    private int run;
    private Gateway destination;
    private int id;

    public Packet(Mote source, Gateway destination, int number, int run) {
        this.source = source;
        this.destination = destination;
        this.number = number;
        this.run = run;
        id = identifier++;
    }

    public Mote getSource() {
        return source;
    }

    public Gateway getDestination() {
        return destination;
    }

    void setDestination(Gateway destination) {
        this.destination = destination;
    }

    public int getStartingRun() {
        return run;
    }

    public Packet clone() {
        return new Packet(source, destination, number, run);
    }

    @Override
    public String toString() {
        return String.format("Source:%d, number:%d", source.getId(), number);
    }

    @Override
    public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + number * time;
//		result = prime * result + ((source == null) ? 0 : source.hashCode());
//		return result;
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Packet other = (Packet) obj;
        if (number != other.number)
            return false;
        if (run != other.run)
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        return true;
    }
}
