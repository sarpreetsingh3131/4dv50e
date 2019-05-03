package domain;

import java.util.*;

public class Mote extends Node {

    private double batteryCapacity;
    private double batteryRemaining;
    private int maxQueueSize;
    private int keepAliveTime;
    private int queueLoss;
    private List<Packet> lostPackets = new LinkedList<>();
    private boolean leafMote = false;
    private int load; // = number of packets to send in a turn
    private Profile<Double> activationProbability = new Constant<>(1.0); // =
    // chance
    // this
    // mote
    // will
    // send
    // packets

    private List<Link> links = new ArrayList<>();
    private Queue<Packet> packetQueue = new LinkedList<>();

    private int lastPacketNumber = 0;

    public Mote(int id, double batteryCapacity, int load, Position position) {
        super(id, position);
        this.batteryCapacity = batteryCapacity;
        this.batteryRemaining = batteryCapacity;
        this.load = load;
    }

    public Mote(int id, double batteryCapacity, int load, int maxQueueSize, int threshold, Position position,
                boolean leafMote) {
        super(id, position);
        this.batteryCapacity = batteryCapacity;
        this.batteryRemaining = batteryCapacity;
        this.load = load;
        this.maxQueueSize = maxQueueSize;
        this.keepAliveTime = threshold;
        this.leafMote = leafMote;
    }

    public Mote(int id, double batteryCapacity, int load) {
        this(id, batteryCapacity, load, null);
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public double getBatteryRemaining() {
        return batteryRemaining;
    }

    public void setBatteryRemaining(double batteryRemaining) {
        this.batteryRemaining = batteryRemaining;
    }

    public int getLoad() {
        return load;
    }

    public Profile<Double> getActivationProbability() {
        return activationProbability;
    }

    public void setActivationProbability(Profile<Double> actProbProf) {
        this.activationProbability = actProbProf;
    }

    public void addLinkTo(Node to, Gateway direction, int power, int distribution) {
        Link link = new Link(this, to, direction, power, distribution);
        links.add(link);
    }

    public Link getLinkTo(Node to) {
        for (Link link : links) {
            if (link.getTo() == to) {
                return link;
            }
        }
        return null;
    }

    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public Queue<Packet> getPacketQueue() {
        return packetQueue;
    }

    /**
     * Handles the turn of this mote for the emulation
     * <p>
     * 1. It might send some packets of it's own (based on load and activation
     * probability) a. It will increment the expected packets for the chosen
     * destination gateway (to calculate packetloss easily) 2. It will send any
     * packets is has queued up from other motes 3. It will reduce its battery based
     * on the number of packets send
     */
    public void handleTurn(RunInfo runInfo, int timeSlots) {
        // System.out.println("Turn: " + this);
        // Create your own packets
        List<Packet> myPackets = new ArrayList<>(); // empty if none send

        // Create your own packets
        // for (int i = 0; i < load; i++) {
        // if (Math.random() < activationProbability.get(runInfo.getRunNumber())) {
        // lastPacketNumber++;
        // myPackets.add(new Packet(this, null, lastPacketNumber,
        // runInfo.getRunNumber()));
        // }
        // }

        // New Logic to create exact packets
        int packetsToGenerate = (int) Math.round(load * activationProbability.get(runInfo.getRunNumber()));
        for (int i = 0; i < packetsToGenerate; i++) {
            lastPacketNumber++;
            myPackets.add(new Packet(this, null, lastPacketNumber, runInfo.getRunNumber()));
        }

        // if (this.getId() == 4)
        // System.out.println(packetQueue.size() + ":" + myPackets.size());

        // Decide what direction to send the packets
        // 1. Calculate total distribution

        int totalDistribution = 0;
        for (Link link : links) {
            totalDistribution += link.getDistribution();
        }
        // 2. For each packet choose a destination
        for (Packet packet : myPackets) {
            int rand = (int) Math.round(Math.random() * totalDistribution);
            int countDistribution = 0;
            for (Link link : links) {
                countDistribution += link.getDistribution();
                if (countDistribution >= rand) {
                    packet.setDestination(link.getDirection());
                    break;
                }
            }
        }

        // 3. Notify the gateways they can expect packages, so they can easily
        // calculate packet loss
        for (Packet packet : myPackets) {
            packet.getDestination().addPacketToExpect(runInfo);
        }

        // Send all packets in queue
        // packetQueue.addAll(myPackets);
        int ownPacketsCount = myPackets.size();
        if (packetQueue.size() + myPackets.size() >= timeSlots) {
            for (int i = 0; i < timeSlots - ownPacketsCount; i++) {
                if (!packetQueue.isEmpty())
                    myPackets.add(packetQueue.remove());
                else
                    break;
            }
        } else {
            // for (int i = 0; i < packetQueue.size(); i++)
            myPackets.addAll(packetQueue);
            packetQueue.clear();
        }

        for (Packet packet : myPackets) {
            // Gather possible destination links
            List<Link> possibleLinks = new ArrayList<>();
            for (Link link : links) {
                if (link.getDirection() == packet.getDestination()) {
                    possibleLinks.add(link);
                }
            }
            // Calculate total distribution
//			int totalDistribution = 0;
//			for (Link link : possibleLinks) {
//				totalDistribution += link.getDistribution();
//			}

            // Send packets
            // if there are no possible links, the packet won't be send.
            if (possibleLinks.isEmpty()) {
                // This if isn't really necessary, if possibleLinks is empty,
                // the last else will happen, but the for-loop won't do anything
                // This if just makes it explicit.
                // TODO perhaps do something else than nothing?
            }
            // if distribution is > 100 I assume packets are duplicated over all
            // possible links
            else if (totalDistribution > 100) {
                for (Link link : possibleLinks) {
                    // sendPacketOver(link, packet, runInfo);
                    link.sendPacket(packet, runInfo);
                }
                if (myPackets.size() > 0) {
                    double batteryUsage = DomainConstants.getPacketDuplicationSfTime()
                            * (DomainConstants.getPacketDuplicationConsumptionRate() / DomainConstants.coulomb);
                    batteryRemaining -= batteryUsage;
                    packet.getDestination().reportPowerConsumed(batteryUsage);
                }
            }
            // else the distribution is handled like weights
            else {
                int rand = (int) Math.round(Math.random() * totalDistribution);
                int countDistribution = 0;
                for (Link link : possibleLinks) {
                    countDistribution += link.getDistribution();
                    // This part assumes that the mote has a maximum of 2 outgoing links
                    if (countDistribution >= rand) {
                        sendPacketOver(link, packet, runInfo);
                        break;
                    }
                }
            }
        }

        // Clear packet queue since they are all send now
        // packetQueue.clear();
        // Check threshold of queue

        Queue<Packet> queue = new LinkedList<>();
        while (!packetQueue.isEmpty()) {
            Packet packet = packetQueue.remove();
            if (runInfo.getRunNumber() - packet.getStartingRun() >= keepAliveTime) {
                // packet.getDestination().reportQueueLoss(packet);
                lostPackets.add(packet);
            } else {
                queue.add(packet);
            }
        }

        queueLoss = lostPackets.size();
        lostPackets.clear();

        packetQueue = queue;

        // Calculate receiving battery
        // TODO: make sure this is the correct behaviour
        links.forEach(link -> link.getTo().calculatePacketReceiveBatteryConsumption(timeSlots));
        // if (totalDistribution > 100) {
        // 	for (Link link : links) {
        // 		link.getTo().calcualtePacketReceiveBatteryConsumption(timeSlots);
        // 	}
        // } else {
        // 	if (links.size() > 0) {
        // 		links.get(0).getTo().calcualtePacketReceiveBatteryConsumption(timeSlots);
        // 	}
        // }
    }

    public int getQueueLoss() {
        return queueLoss;
    }

    public void resetQueueLoss() {
        lostPackets.clear();
    }

    void sendPacketOver(Link link, Packet packet, RunInfo runInfo) {
        assert links.contains(link);
        // System.out.println("Sending packet to:" + packet + " to " +
        // link.getTo().getId());
        // Send packet over a link
        link.sendPacket(packet, runInfo);

        // Subtract battery usage
        double batteryUsage = link.getSfTime() * (link.getPowerConsumptionRate() / DomainConstants.coulomb);
        batteryRemaining -= batteryUsage;
        packet.getDestination().reportPowerConsumed(batteryUsage);

    }

    @Override
    void receivePacket(Packet packet, RunInfo runInfo) {
        if (packetQueue.size() < maxQueueSize) {
            if (!packetQueue.contains(packet))
                packetQueue.add(packet);
        } else {
            lostPackets.add(packet);
            packet.getDestination().reportQueueLoss(packet);
        }
        // Subtract battery life
        // double batteryUsage = DomainConstants.receptionTime *
        // (DomainConstants.receptionCost / DomainConstants.coulomb);
        // batteryRemaining -= batteryUsage;
        // packet.getDestination().reportPowerConsumed(batteryUsage);
    }

    @Override
    public String toString() {
        return "Mote " + String.format("%2d", getId()) + " [battery=" + String.format("%5.1f", batteryRemaining) + "/"
                + String.format("%5.1f", batteryCapacity) + ", load=" + load + ", queue=" + packetQueue.size() + "]";
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public int getQueueSize() {
        return packetQueue.size();
    }

    @Override
    void calculatePacketReceiveBatteryConsumption(int timeSlots) {
        assert links.size() >= 1;

        double batteryUsage = timeSlots * DomainConstants.receptionTime
                * (DomainConstants.receptionCost / DomainConstants.coulomb);
        batteryRemaining -= batteryUsage;

        links.get(0).getDirection().reportPowerConsumed(batteryUsage);

    }
}
