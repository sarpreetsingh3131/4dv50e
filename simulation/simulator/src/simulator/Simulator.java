package simulator;

import deltaiot.services.QoS;
import domain.*;

import java.util.*;

public class Simulator {

    private List<Mote> motes = new ArrayList<>();
    private List<Gateway> gateways = new ArrayList<>();
    private List<Integer> turnOrder = new ArrayList<>();
    private int MaxTimeSlots;
    private RunInfo runInfo = new RunInfo();
    private List<QoS> qosValues = new ArrayList<>();
    private boolean packetDuplication;

    // Constructor
    public Simulator() {
    }

    public static Simulator createBaseCase() {
        Simulator simul = new Simulator();

        // Motes
        double battery = 11880;
        int load = 10;
        Mote mote1 = new Mote(1, battery, load);
        Mote mote12 = new Mote(12, battery, load);
        Mote mote2 = new Mote(2, battery, load);
        simul.addMotes(mote1, mote12, mote2);

        // Gateways
        // I use the convention to give gateways negative ids
        // Nothing enforces this, but all ids have to be unique between all nodes (=
        // motes & gateways)
        Gateway gateway1 = new Gateway(-1);
        gateway1.setView(mote1, mote12);
        Gateway gateway2 = new Gateway(-2);
        gateway2.setView(mote2, mote12);
        simul.addGateways(gateway1, gateway2);

        // Links
        int power = 15;
        int distribution = 100;
        mote1.addLinkTo(gateway1, gateway1, power, distribution);
        mote2.addLinkTo(gateway2, gateway2, power, distribution);
        mote12.addLinkTo(mote1, gateway1, power, distribution);
        mote12.addLinkTo(mote2, gateway2, power, distribution);

        simul.setTurnOrder(mote12, mote1, mote2);

        return simul;
    }

    public static Simulator createBaseCase2() {
        Simulator simul = new Simulator();

        // Motes
        double battery = 11880;
        int load = 10;
        Mote mote0 = new Mote(0, battery, load);
        Mote mote11 = new Mote(11, battery, load);
        Mote mote12 = new Mote(12, battery, load);
        Mote mote21 = new Mote(21, battery, load);
        Mote mote22 = new Mote(22, battery, load);
        simul.addMotes(mote0, mote11, mote12, mote21, mote22);

        // Gateways
        // I use the convention to give gateways negative ids
        // Nothing enforces this, but all ids have to be unique between all nodes (=
        // motes & gateways)
        Gateway gateway1 = new Gateway(-1);
        gateway1.setView(mote11, mote12, mote0);
        Gateway gateway2 = new Gateway(-2);
        gateway2.setView(mote21, mote22, mote0);
        simul.addGateways(gateway1, gateway2);

        // Links
        int power = 15;
        int distribution = 100;
        mote0.addLinkTo(mote11, gateway1, power, distribution);
        mote0.addLinkTo(mote12, gateway1, power, distribution);
        mote0.addLinkTo(mote21, gateway2, power, distribution);
        mote0.addLinkTo(mote22, gateway2, power, distribution);

        mote11.addLinkTo(gateway1, gateway1, power, distribution);
        mote12.addLinkTo(gateway1, gateway1, power, distribution);

        mote21.addLinkTo(gateway2, gateway2, power, distribution);
        mote22.addLinkTo(gateway2, gateway2, power, distribution);

        simul.setTurnOrder(mote0, mote11, mote12, mote21, mote22);

        return simul;
    }

    // Pre-build simulators

    public boolean isPacketDuplication() {
        return packetDuplication;
    }

    public void setPacketDuplication(boolean packetDuplication) {
        this.packetDuplication = packetDuplication;
    }

    // Creation API

    public void addMotes(Mote... motes) {
        Collections.addAll(this.motes, motes);
    }

    public void addGateways(Gateway... gateways) {
        Collections.addAll(this.gateways, gateways);
    }

    /**
     * Do a single simulation run. This will simulate the sending of packets through
     * the network to the gateways. Each gateway will aggregate information about
     * packet-loss and power-consumption. To get this information, use
     * gateway.calculatePacketLoss and gateway.getPowerConsumed respectively.
     */
    public void doSingleRun() {
        // Reset the gateways aggregated values, so we can start a new window to see
        // packet loss and power consumption
        resetGatewaysAggregatedValues();

        // Do the actual run, this will give all motes a turn
        // Give each mote a turn, in the given order
        for (Integer id : turnOrder) {
            Mote mote = getMoteWithId(id);
            // Let mote handle its turn
            mote.handleTurn(runInfo, MaxTimeSlots); // return value doesn't include packets send for other motes, only
            // its own packets
        }

        // QoS
        QoS qos = new QoS();
        qos.setEnergyConsumption(gateways.get(0).getPowerConsumed());

        List<Packet> queuePackets = new LinkedList<Packet>();
        for (Mote mote : gateways.get(0).getView()) {
            queuePackets.addAll(mote.getPacketQueue());
        }

        qos.setPacketLoss(gateways.get(0).calculatePacketLoss(runInfo, packetDuplication));
        qos.setLatency(gateways.get(0).calculateLatency(runInfo));
        int queueLoss = 0;
        for (Mote mote : gateways.get(0).getView()) {
            queueLoss += mote.getQueueLoss();
            mote.resetQueueLoss();
        }

        qos.setQueueLoss(queueLoss / (double) gateways.get(0).getExpectedPackets(runInfo));
        qos.setSent((double) gateways.get(0).getExpectedPackets(runInfo));
        qos.setPeriod("" + runInfo.getRunNumber());
        qosValues.add(qos);

        // Increase run number
        runInfo.incrementRunNumber();
    }

    private void resetGatewaysAggregatedValues() {
        // Reset gateways' packetstore and expected packet count, so the packetloss for
        // this run can be calculated easily
        // Also reset the consumed power, so this is correctly aggregated for this run
        for (Gateway gateway : gateways) {
            gateway.resetPacketStore();
            int queuedPackets = 0;
            for (Mote mote : gateways.get(0).getView()) {
                queuedPackets += mote.getPacketQueue().size();
            }
            // gateway.setQueuedPacketsToExpectedPackets();
            // gateway.setExpectedPackets(queuedPackets);
            // gateway.resetPacketStoreAndExpectedPacketCount();
            // gateway.resetQueueLoss();
            // gateway.resetQueuePackets();
            gateway.resetPowerConsumed();
        }
    }

    // Simulation API

    public Mote getMoteWithId(int id) {
        for (Mote mote : motes) {
            if (mote.getId() == id)
                return mote;
        }
        return null;
    }

    public Gateway getGatewayWithId(int id) {
        for (Gateway gw : gateways) {
            if (gw.getId() == id)
                return gw;
        }
        return null;
    }

    // Alteration and inspection API

    public List<Integer> getTurnOrder() {
        return Collections.unmodifiableList(turnOrder);
    }

    public void setTurnOrder(Mote... motes) {
        Integer[] ids = new Integer[motes.length];
        for (int i = 0; i < motes.length; ++i) {
            ids[i] = motes[i].getId();
        }
        setTurnOrder(ids);
    }

    public void setTurnOrder(Integer... ids) {
        this.turnOrder = Arrays.asList(ids);
    }

    /**
     * Gets the Node with a specified id if one exists This can be both a Mote or a
     * Gateway
     *
     * @param id The id
     * @return The node with the given id (either a mote or gateway) if one exists
     * (null otherwise)
     */
    public Node getNodeWithId(int id) {
        Mote mote = getMoteWithId(id);
        if (mote == null) {
            Gateway gw = getGatewayWithId(id);
            return gw;
        } else
            return mote;
    }

    public List<Gateway> getGateways() {
        return Collections.unmodifiableList(gateways);
    }

    public List<Mote> getMotes() {
        return Collections.unmodifiableList(motes);
    }

    public RunInfo getRunInfo() {
        return runInfo;
    }

    public List<QoS> getQosValues() {
        return qosValues;
    }

    public int getMaxTimeSlots() {
        return MaxTimeSlots;
    }

    public void setMaxTimeSlots(int maxTimeSlots) {
        MaxTimeSlots = maxTimeSlots;
    }
}
