package domain;

public abstract class Node {

    private int id;
    private Profile<Double> interference = new Constant<>(0.0);
    private Position position;

    // Constructors
    public Node(int id, Position position) {
        this.id = id;
        this.position = position;
    }

    abstract void calculatePacketReceiveBatteryConsumption(int timeSlots);

    // Functionality
    abstract void receivePacket(Packet packet, RunInfo runInfo);

    // Getters and setters
    public int getId() {
        return id;
    }

    public Profile<Double> getInterference() {
        return interference;
    }

    public void setInterference(Profile<Double> interference) {
        this.interference = interference;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
