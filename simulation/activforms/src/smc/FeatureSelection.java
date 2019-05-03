package smc;

import mapek.*;
import org.json.JSONArray;
import util.ConfigLoader;
import util.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
public class FeatureSelection {
    private String network;

    // The features which should be sent to the learners
    // Pairs represent links, single integers represent motes
    private Map<String, List<Pair<Integer, Integer>>> selectedSNR = new HashMap<>();
    private Map<String, List<Pair<Integer, Integer>>> selectedPower = new HashMap<>();
    private Map<String, List<Pair<Integer, Integer>>> selectedDist = new HashMap<>();
    private Map<String, List<Integer>> selectedLoad = new HashMap<>();

    public FeatureSelection() {
        network = ConfigLoader.getInstance().getSimulationNetwork();

        // Initialisation of selected features for DeltaIoTv1
        selectedSNR.put("DeltaIoTv1", Arrays.asList(
                new Pair<>(2, 4),
                new Pair<>(4, 1),
                new Pair<>(5, 9),
                new Pair<>(6, 4),
                new Pair<>(7, 3),
                new Pair<>(10, 5), new Pair<>(10, 6),
                new Pair<>(12, 3), new Pair<>(12, 7),
                new Pair<>(14, 12),
                new Pair<>(15, 12)
        ));
        selectedPower.put("DeltaIoTv1", Arrays.asList(
                new Pair<>(3, 1),
                new Pair<>(4, 1),
                new Pair<>(7, 2),
                new Pair<>(8, 1),
                new Pair<>(9, 1),
                new Pair<>(10, 5), new Pair<>(10, 6),
                new Pair<>(11, 7),
                new Pair<>(13, 11)
        ));
        selectedDist.put("DeltaIoTv1", Arrays.asList(
                new Pair<>(7, 2), new Pair<>(7, 3),
                new Pair<>(10, 5), new Pair<>(10, 6),
                new Pair<>(12, 3), new Pair<>(12, 7)
        ));
        selectedLoad.put("DeltaIoTv1", Arrays.asList(10, 13));

        // Initialisation of selected features for DeltaIoTv2
        selectedSNR.put("DeltaIoTv2", Arrays.asList(
                new Pair<>(2, 3),
                new Pair<>(3, 4),
                new Pair<>(3, 6),
                new Pair<>(6, 12),
                new Pair<>(9, 2),
                new Pair<>(13, 14),
                new Pair<>(14, 26),
                new Pair<>(16, 19),
                new Pair<>(19, 18),
                new Pair<>(22, 23),
                new Pair<>(24, 21),
                new Pair<>(25, 10),
                new Pair<>(26, 15),
                new Pair<>(28, 20),
                new Pair<>(29, 20),
                new Pair<>(31, 1),
                new Pair<>(33, 29),
                new Pair<>(34, 33),
                new Pair<>(35, 30),
                new Pair<>(36, 32),
                new Pair<>(37, 32)
        ));
        selectedPower.put("DeltaIoTv2", Arrays.asList(
                new Pair<>(4, 5),
                new Pair<>(5, 1),
                new Pair<>(6, 5),
                new Pair<>(6, 12),
                new Pair<>(7, 22),
                new Pair<>(8, 21),
                new Pair<>(9, 2),
                new Pair<>(10, 11),
                new Pair<>(11, 12),
                new Pair<>(12, 1),
                new Pair<>(14, 25),
                new Pair<>(14, 26),
                new Pair<>(15, 10),
                new Pair<>(16, 17),
                new Pair<>(16, 19),
                new Pair<>(17, 18),
                new Pair<>(18, 1),
                new Pair<>(20, 1),
                new Pair<>(21, 1),
                new Pair<>(22, 21),
                new Pair<>(22, 23),
                new Pair<>(23, 21),
                new Pair<>(24, 21),
                new Pair<>(27, 28),
                new Pair<>(28, 20),
                new Pair<>(30, 31),
                new Pair<>(32, 31),
                new Pair<>(35, 27),
                new Pair<>(35, 30)
        ));
        selectedDist.put("DeltaIoTv2", Arrays.asList(
                new Pair<>(3, 4), new Pair<>(3, 6),
                new Pair<>(6, 5), new Pair<>(6, 12),
                new Pair<>(14, 25), new Pair<>(14, 26),
                new Pair<>(16, 17), new Pair<>(16, 19),
                new Pair<>(22, 21), new Pair<>(22, 23),
                new Pair<>(35, 27), new Pair<>(35, 30)
        ));
        selectedLoad.put("DeltaIoTv2", Arrays.asList(10, 23));
    }

    public JSONArray selectFeatures(AdaptationOption option, Environment env) {
        if (!selectedSNR.containsKey(network)) {
            throw new RuntimeException(String.format("Unsupported network for feature selection: %s", network));
        }

        JSONArray features = new JSONArray();

        // Add the SNR values of certain links in the environment
        for (SNR snr : env.linksSNR) {
            if (selectedSNR.get(network).stream()
                    .anyMatch(l -> l.first == snr.source && l.second == snr.destination)) {
                features.put((int) snr.SNR);
            }
        }

        // Add the power settings for certain links
        for (Mote mote : option.system.motes.values()) {
            for (Link link : mote.getLinks()) {
                if (selectedPower.get(network).stream()
                        .anyMatch(l -> l.first == link.getSource() && l.second == link.getDestination())) {
                    features.put((int) link.getPower());
                }
            }
        }

        // Add the distribution values for certain links (links from motes with 2 parents)
        for (Mote mote : option.system.motes.values()) {
            for (Link link : mote.getLinks()) {
                if (selectedDist.get(network).stream()
                        .anyMatch(l -> l.first == link.getSource() && l.second == link.getDestination())) {
                    features.put((int) link.getDistribution());
                }
            }
        }

        // Add the load for motes 10 and 12
        for (TrafficProbability traffic : env.motesLoad) {
            if (selectedLoad.get(network).stream().anyMatch(o -> o == traffic.moteId)) {
                features.put((int) traffic.load);
            }
        }

        return features;
    }
}
    
*/