package simulator;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.SimulationClient;
import deltaiot.services.QoS;

import java.util.ArrayList;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        ArrayList<ArrayList<deltaiot.services.Mote>> adaptations = new ArrayList<>();
        ArrayList<ArrayList<QoS>> result = new ArrayList<>();
        Random ran = new Random();
        int[] powers = new int[17];
        int[] distributions = new int[6];
        double globalInterference = -3;
        int probability1 = 0;
        int probability2 = 0;

        // for (int a = 0; a < 1000; a++)
        while (globalInterference < 10) {

            for (int b = 0; b < powers.length; b++) {
                powers[b] = ran.nextInt(101) % 16;
            }

            for (int c = 0; c < distributions.length; c++) {
                distributions[c] = c == 1 ? 100 : 0;
            }

            probability1 = ran.nextInt(101);
            probability2 = ran.nextInt(101);

            for (int i = 0; i < 6; i++) {
                distributions[2] = 0;
                distributions[3] = 100;

                for (int j = 0; j < 6; j++) {
                    distributions[4] = 0;
                    distributions[5] = 100;

                    for (int k = 0; k < 6; k++) {
                        Simulator simul = DeltaIoTSimulator.createSimulatorForTraining(distributions, powers,
                                globalInterference, probability1, probability2);

                        SimulationClient client = new SimulationClient(simul);
                        // for (int m = 0; m < 10; m++) {
                        adaptations.add(client.getProbe().getAllMotes());
                        System.out.println(adaptations.get(0));
                        result.add(client.getNetworkQoS(1));
                        System.out.println(client.getNetworkQoS(1));
                        // }
                        distributions[4] += 20;
                        distributions[5] -= 20;
                    }
                    distributions[2] += 20;
                    distributions[3] -= 20;
                }
                distributions[0] += 20;
                distributions[1] -= 20;
            }
            globalInterference += 0.01;
        }
    }
}