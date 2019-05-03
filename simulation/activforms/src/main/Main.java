package main;

import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.client.SimulationClient;
import deltaiot.services.QoS;
import domain.Link;
import domain.Mote;
import mapek.FeedbackLoop;
import mapek.SNREquation;
import simulator.Simulator;
import util.ConfigLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    Probe probe;
    Effector effector;
    Simulator simulator;

    public static void main(String[] args) {
        Main ddaptation = new Main();
        ddaptation.initializeSimulator();
        ddaptation.start();
    }

    public void start() {

        new Thread(() -> {
            // Compile the list of SNREquations for all the links in the simulator
            List<SNREquation> equations = new ArrayList<>();

            // Firstly, assemble all the links in the simulator
            List<Link> links = simulator.getMotes().stream()
                    .map(Mote::getLinks)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            // Secondly, loop over all the links, and add their SNREquations to the overall list
            for (Link link : links) {
                equations.add(new SNREquation(link.getFrom().getId(),
                        link.getTo().getId(),
                        link.getSnrEquation().multiplier,
                        link.getSnrEquation().constant));
            }

            // Start a new feedback loop
            FeedbackLoop feedbackLoop = new FeedbackLoop();
            feedbackLoop.setProbe(probe);
            feedbackLoop.setEffector(effector);
            feedbackLoop.setEquations(equations);

            // StartFeedback loop (this runs for the amount of cycles specified in the configuration)
            feedbackLoop.start();

            //printResults();

        }).start();
    }

    void printResults() {
        // Get QoS data of previous runs
        // probe.getNetworkQoS() should not have less number than the number of times
        // feedback loop will run, e.g, feedback loop runs 5 times, this should have >=5
        List<QoS> qosList = probe.getNetworkQoS(ConfigLoader.getInstance().getAmountOfCycles());
        System.out.println("\nPacketLoss;Latency;EnergyConsumption");
        for (QoS qos : qosList) {
            System.out.println(String.format("%f;%f;%f", qos.getPacketLoss(), qos.getLatency(), qos.getEnergyConsumption()));
        }
    }

    // Initialises a new simulator and probe
    public void initializeSimulator() {
        String simulationNetwork = ConfigLoader.getInstance().getSimulationNetwork();

        // Start a completely new sim
        SimulationClient client = new SimulationClient(simulationNetwork);

        // Assign a new probe, effector and simulator to the main object.
        probe = client.getProbe();
        effector = client.getEffector();
        simulator = client.getSimulator();
    }

    public Simulator getSimulator() {
        return simulator;
    }
}
