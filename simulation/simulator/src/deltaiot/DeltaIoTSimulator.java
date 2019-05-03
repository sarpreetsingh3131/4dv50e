package deltaiot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import domain.*;
import simulator.Simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeltaIoTSimulator {

    final static int GATEWAY_ID = 1;
    final static int PORT = 9888;
    final Lock lock = new ReentrantLock();
    final Condition adaptationCompleted = lock.newCondition();
    Simulator simul;

    public DeltaIoTSimulator() {
        // this.simul = createSimulatorForDeltaIoT();
        this.simul = createSimulatorForDeltaIoTv2();
    }

    public static Simulator createSimulatorForDeltaIoT() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 60;
        int keepAliveTime = 1;
        int maxQueueSlots = 40;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        Mote mote6 = new Mote(6, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote7 = new Mote(7, battery, load, maxQueueSize, keepAliveTime,
                new Position(324 * posScale, 273 * posScale), false);
        Mote mote8 = new Mote(8, battery, load, maxQueueSize, keepAliveTime,
                new Position(392 * posScale, 478 * posScale), true);
        Mote mote9 = new Mote(9, battery, load, maxQueueSize, keepAliveTime,
                new Position(540 * posScale, 479 * posScale), false);
        Mote mote10 = new Mote(10, battery, load, maxQueueSize, keepAliveTime,
                new Position(694 * posScale, 356 * posScale), true);
        Mote mote11 = new Mote(11, battery, load, maxQueueSize, keepAliveTime,
                new Position(234 * posScale, 232 * posScale), false);
        Mote mote12 = new Mote(12, battery, load, maxQueueSize, keepAliveTime,
                new Position(221 * posScale, 322 * posScale), false);
        Mote mote13 = new Mote(13, battery, load, maxQueueSize, keepAliveTime,
                new Position(142 * posScale, 170 * posScale), true);
        Mote mote14 = new Mote(14, battery, load, maxQueueSize, keepAliveTime,
                new Position(139 * posScale, 293 * posScale), true);
        Mote mote15 = new Mote(15, battery, load, maxQueueSize, keepAliveTime,
                new Position(128 * posScale, 344 * posScale), true);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7, mote8, mote9, mote10, mote11, mote12,
                mote13, mote14, mote15};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = false;
        simul.setPacketDuplication(packetDuplication);
        mote2.addLinkTo(mote4, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(gateway, gateway, power, distribution);
        mote5.addLinkTo(mote9, gateway, power, distribution);
        mote6.addLinkTo(mote4, gateway, power, distribution);
        mote7.addLinkTo(mote2, gateway, power, packetDuplication ? 100 : 50);
        mote7.addLinkTo(mote3, gateway, power, packetDuplication ? 100 : 50);
        mote8.addLinkTo(gateway, gateway, power, distribution);
        mote9.addLinkTo(gateway, gateway, power, distribution);
        mote10.addLinkTo(mote6, gateway, power, packetDuplication ? 100 : 50);
        mote10.addLinkTo(mote5, gateway, power, packetDuplication ? 100 : 50);
        mote11.addLinkTo(mote7, gateway, power, distribution);
        mote12.addLinkTo(mote7, gateway, power, packetDuplication ? 100 : 50);
        mote12.addLinkTo(mote3, gateway, power, packetDuplication ? 100 : 50);
        mote13.addLinkTo(mote11, gateway, power, distribution);
        mote14.addLinkTo(mote12, gateway, power, distribution);
        mote15.addLinkTo(mote12, gateway, power, distribution);

        // Set order
        simul.setTurnOrder(8, 10, 13, 14, 15, 5, 6, 11, 12, 9, 7, 2, 3, 4);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 2,4,8,10,13,14 Passive infrared sensors
        mote2.setActivationProbability(new Constant<>(0.50));
        mote4.setActivationProbability(new Constant<>(0.50));
        mote8.setActivationProbability(new Constant<>(0.50));
        mote10.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));
        mote13.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR2.txt", 1.0));
        mote14.setActivationProbability(new Constant<>(0.50));

        // 5,7,11,12 RFID sensors
        mote5.setActivationProbability(new Constant<>(0.50));
        mote7.setActivationProbability(new Constant<>(0.50));
        mote11.setActivationProbability(new Constant<>(0.50));
        mote12.setActivationProbability(new Constant<>(0.50));


        mote10.getLinkTo(mote6).setInterference(new FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));
        mote12.getLinkTo(mote3).setInterference(new FileProfile("deltaiot/scenario_data/SNR2.txt", 0.0));

        // Add SNR equations new settings
        mote2.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0169, 7.4076));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.4982, 1.2468));
        mote4.getLinkTo(gateway).setSnrEquation(new SNREquation(0.8282, -8.1246));
        mote5.getLinkTo(mote9).setSnrEquation(new SNREquation(0.4932, -4.4898));
        mote6.getLinkTo(mote4).setSnrEquation(new SNREquation(0.6199, -9.8051));
        mote7.getLinkTo(mote3).setSnrEquation(new SNREquation(0.5855, -6.644));
        mote7.getLinkTo(mote2).setSnrEquation(new SNREquation(0.5398, -2.0549));
        mote8.getLinkTo(gateway).setSnrEquation(new SNREquation(0.5298, -0.1031));
        mote9.getLinkTo(gateway).setSnrEquation(new SNREquation(0.8284, -7.2893));
        mote10.getLinkTo(mote6).setSnrEquation(new SNREquation(0.8219, -7.3331));
        mote10.getLinkTo(mote5).setSnrEquation(new SNREquation(0.6463, -3.0037));
        mote11.getLinkTo(mote7).setSnrEquation(new SNREquation(0.714, -3.1985));
        mote12.getLinkTo(mote7).setSnrEquation(new SNREquation(0.9254, -16.21));
        mote12.getLinkTo(mote3).setSnrEquation(new SNREquation(0.1, 6));
        mote13.getLinkTo(mote11).setSnrEquation(new SNREquation(0.6078, -3.6005));
        mote14.getLinkTo(mote12).setSnrEquation(new SNREquation(0.4886, -4.7704));
        mote15.getLinkTo(mote12).setSnrEquation(new SNREquation(0.5899, -7.1896));

        // Add SNR equations (from Usman's settings class)
        // mote2 .getLinkTo(mote4 ).setSnrEquation(new SNREquation(
        // 0.0473684210526, -5.29473684211));
        // mote3 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.0280701754386, 4.25263157895));
        // mote4 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.119298245614, -1.49473684211));
        // mote5 .getLinkTo(mote9 ).setSnrEquation(new
        // SNREquation(-0.019298245614, 4.8));
        // mote6 .getLinkTo(mote4 ).setSnrEquation(new SNREquation(
        // 0.0175438596491, -3.84210526316));
        // mote7 .getLinkTo(mote3 ).setSnrEquation(new SNREquation(
        // 0.168421052632, 2.30526315789));
        // mote7 .getLinkTo(mote2 ).setSnrEquation(new
        // SNREquation(-0.0157894736842, 3.77894736842));
        // mote8 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.00350877192982, 0.45263157895));
        // mote9 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.0701754385965, 2.89473684211));
        // mote10.getLinkTo(mote6 ).setSnrEquation(new SNREquation(
        // 3.51139336547e-16, -2.21052631579));
        // mote10.getLinkTo(mote5 ).setSnrEquation(new SNREquation(
        // 0.250877192982, -1.75789473684));
        // mote11.getLinkTo(mote7 ).setSnrEquation(new SNREquation(
        // 0.380701754386, -2.12631578947));
        // mote12.getLinkTo(mote7 ).setSnrEquation(new SNREquation(
        // 0.317543859649, 2.95789473684));
        // mote12.getLinkTo(mote3 ).setSnrEquation(new
        // SNREquation(0.0157894736842, -3.77894736842));
        // mote13.getLinkTo(mote11 ).setSnrEquation(new
        // SNREquation(0.0210526315789, -2.81052631579));
        // mote14.getLinkTo(mote12 ).setSnrEquation(new SNREquation(
        // 0.0333333333333, 2.58947368421));
        // mote15.getLinkTo(mote12 ).setSnrEquation(new SNREquation(
        // 0.0438596491228, 1.31578947368));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new DoubleRange(3.0, 6.0));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    public static Simulator createSimulatorForDeltaIoTv2() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 80;
        int keepAliveTime = 1;
        int maxQueueSlots = 60;
        double posScale = 2;

        List<Integer> leafMotes = Arrays.asList(7, 9, 13, 16, 24, 34, 35, 36, 37);
        // List<Integer> leafMotes = List.of(7, 9, 13, 16, 24, 34, 35, 36, 37);
        List<Mote> motes = new ArrayList<>();
        for (int i = 2; i < 38; i++) {
            motes.add(new Mote(i, battery, load, maxQueueSize, keepAliveTime, new Position(i * posScale, i * posScale), leafMotes.contains(i)));
        }
        Mote[] allMotes = new Mote[motes.size()];
        allMotes = motes.toArray(allMotes);
        simul.addMotes(allMotes);

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(1 * posScale, 1 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = false;
        simul.setPacketDuplication(packetDuplication);
        allMotes[2 - 2].addLinkTo(allMotes[3 - 2], gateway, power, distribution);
        allMotes[3 - 2].addLinkTo(allMotes[4 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[3 - 2].addLinkTo(allMotes[6 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[4 - 2].addLinkTo(allMotes[5 - 2], gateway, power, distribution);
        allMotes[5 - 2].addLinkTo(gateway, gateway, power, distribution);
        allMotes[6 - 2].addLinkTo(allMotes[5 - 2], gateway, power, distribution);
        allMotes[6 - 2].addLinkTo(allMotes[12 - 2], gateway, power, distribution);
        // allMotes[7-2].addLinkTo(allMotes[8-2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[7 - 2].addLinkTo(allMotes[22 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[8 - 2].addLinkTo(allMotes[21 - 2], gateway, power, distribution);
        allMotes[9 - 2].addLinkTo(allMotes[2 - 2], gateway, power, distribution);
        allMotes[10 - 2].addLinkTo(allMotes[11 - 2], gateway, power, distribution);
        allMotes[11 - 2].addLinkTo(allMotes[12 - 2], gateway, power, distribution);
        allMotes[12 - 2].addLinkTo(gateway, gateway, power, distribution);
        allMotes[13 - 2].addLinkTo(allMotes[14 - 2], gateway, power, distribution);
        allMotes[14 - 2].addLinkTo(allMotes[25 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[14 - 2].addLinkTo(allMotes[26 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[15 - 2].addLinkTo(allMotes[10 - 2], gateway, power, distribution);
        allMotes[16 - 2].addLinkTo(allMotes[17 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[16 - 2].addLinkTo(allMotes[19 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[17 - 2].addLinkTo(allMotes[18 - 2], gateway, power, distribution);
        allMotes[18 - 2].addLinkTo(gateway, gateway, power, distribution);
        allMotes[19 - 2].addLinkTo(allMotes[18 - 2], gateway, power, distribution);
        allMotes[20 - 2].addLinkTo(gateway, gateway, power, distribution);
        // allMotes[20-2].addLinkTo(allMotes[32-2], gateway, power, distribution);
        allMotes[21 - 2].addLinkTo(gateway, gateway, power, distribution);
        allMotes[22 - 2].addLinkTo(allMotes[21 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[22 - 2].addLinkTo(allMotes[23 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[23 - 2].addLinkTo(allMotes[21 - 2], gateway, power, distribution);
        allMotes[24 - 2].addLinkTo(allMotes[21 - 2], gateway, power, distribution);
        allMotes[25 - 2].addLinkTo(allMotes[10 - 2], gateway, power, distribution);
        allMotes[26 - 2].addLinkTo(allMotes[15 - 2], gateway, power, distribution);
        allMotes[27 - 2].addLinkTo(allMotes[28 - 2], gateway, power, distribution);
        allMotes[28 - 2].addLinkTo(allMotes[20 - 2], gateway, power, distribution);
        allMotes[29 - 2].addLinkTo(allMotes[20 - 2], gateway, power, distribution);
        allMotes[30 - 2].addLinkTo(allMotes[31 - 2], gateway, power, distribution);
        allMotes[31 - 2].addLinkTo(gateway, gateway, power, distribution);
        allMotes[32 - 2].addLinkTo(allMotes[31 - 2], gateway, power, distribution);
        allMotes[33 - 2].addLinkTo(allMotes[29 - 2], gateway, power, distribution);
        allMotes[34 - 2].addLinkTo(allMotes[33 - 2], gateway, power, distribution);
        allMotes[35 - 2].addLinkTo(allMotes[27 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[35 - 2].addLinkTo(allMotes[30 - 2], gateway, power, packetDuplication ? 100 : 50);
        allMotes[36 - 2].addLinkTo(allMotes[32 - 2], gateway, power, distribution);
        allMotes[37 - 2].addLinkTo(allMotes[32 - 2], gateway, power, distribution);

        // Set order
        simul.setTurnOrder(7, 9, 13, 16, 24, 34, 35, 36, 37, 2, 8, 14, 17, 19, 22, 27, 30, 32, 33, 3, 18, 23, 25, 26, 28, 29, 31, 4, 6, 15, 20, 21, 5, 10, 11, 12);

        // Activation probabilites for all the motes
        int[] heatSensors = {2, 4, 7, 9, 13, 15, 17, 18, 21, 22, 28, 31, 34};
        int[] RFIDSensors = {3, 6, 8, 12, 14, 20, 24, 26, 30, 35, 36, 37};
        int[] infraredSensors = {5, 10, 11, 16, 19, 23, 25, 27, 29, 32, 33};
        for (int i : infraredSensors) {
            allMotes[i - 2].setActivationProbability(new Constant<>(0.50));
        }
        for (int i : RFIDSensors) {
            allMotes[i - 2].setActivationProbability(new Constant<>(0.50));
        }


        allMotes[10 - 2].setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));
        allMotes[23 - 2].setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR2.txt", 1.0));


        // Interference on all the links
        allMotes[2 - 2].getLinkTo(allMotes[3 - 2]).setSnrEquation(new SNREquation(0.7231, -7.4954));
        allMotes[3 - 2].getLinkTo(allMotes[4 - 2]).setSnrEquation(new SNREquation(0.2906, -5.9390));
        allMotes[3 - 2].getLinkTo(allMotes[6 - 2]).setSnrEquation(new SNREquation(0.4548, 3.0171));
        allMotes[4 - 2].getLinkTo(allMotes[5 - 2]).setSnrEquation(new SNREquation(0.3905, -0.4642));
        allMotes[5 - 2].getLinkTo(gateway).setSnrEquation(new SNREquation(0.6294, -3.9005));
        allMotes[6 - 2].getLinkTo(allMotes[5 - 2]).setSnrEquation(new SNREquation(0.4571, -1.4886));
        // allMotes[7-2].getLinkTo(allMotes[8-2]).setSnrEquation(new SNREquation(0.4085,-2.6870));
        allMotes[6 - 2].getLinkTo(allMotes[12 - 2]).setSnrEquation(new SNREquation(0.4085, -2.6870));
        allMotes[7 - 2].getLinkTo(allMotes[22 - 2]).setSnrEquation(new SNREquation(1.0000, -5.0489));
        allMotes[8 - 2].getLinkTo(allMotes[21 - 2]).setSnrEquation(new SNREquation(0.3323, -0.4125));
        allMotes[9 - 2].getLinkTo(allMotes[2 - 2]).setSnrEquation(new SNREquation(0.4602, 5.4036));
        allMotes[10 - 2].getLinkTo(allMotes[11 - 2]).setSnrEquation(new SNREquation(0.6508, -2.5972));
        allMotes[11 - 2].getLinkTo(allMotes[12 - 2]).setSnrEquation(new SNREquation(0.3563, -0.5603));
        allMotes[12 - 2].getLinkTo(gateway).setSnrEquation(new SNREquation(0.6803, -5.2136));
        allMotes[13 - 2].getLinkTo(allMotes[14 - 2]).setSnrEquation(new SNREquation(0.2850, -2.1999));
        allMotes[14 - 2].getLinkTo(allMotes[25 - 2]).setSnrEquation(new SNREquation(0.6745, -2.1041));
        allMotes[14 - 2].getLinkTo(allMotes[26 - 2]).setSnrEquation(new SNREquation(0.4489, -5.9915));
        allMotes[15 - 2].getLinkTo(allMotes[10 - 2]).setSnrEquation(new SNREquation(0.6656, -3.4782));
        allMotes[16 - 2].getLinkTo(allMotes[17 - 2]).setSnrEquation(new SNREquation(0.5635, -1.7281));
        allMotes[16 - 2].getLinkTo(allMotes[19 - 2]).setSnrEquation(new SNREquation(0.5041, -6.7458));
        allMotes[17 - 2].getLinkTo(allMotes[18 - 2]).setSnrEquation(new SNREquation(0.6706, 3.1184));
        allMotes[18 - 2].getLinkTo(gateway).setSnrEquation(new SNREquation(0.6432, 3.0667));
        allMotes[19 - 2].getLinkTo(allMotes[18 - 2]).setSnrEquation(new SNREquation(0.2954, -5.5948));
        allMotes[20 - 2].getLinkTo(gateway).setSnrEquation(new SNREquation(0.5469, -0.5889));
        // allMotes[20-2].getLinkTo(allMotes[32-2]).setSnrEquation(new SNREquation(0.6085,-2.6870));
        allMotes[21 - 2].getLinkTo(gateway).setSnrEquation(new SNREquation(0.4759, -1.9652));
        allMotes[22 - 2].getLinkTo(allMotes[21 - 2]).setSnrEquation(new SNREquation(0.5604, -2.2108));
        allMotes[22 - 2].getLinkTo(allMotes[23 - 2]).setSnrEquation(new SNREquation(0.1332, -4.0037));
        allMotes[23 - 2].getLinkTo(allMotes[21 - 2]).setSnrEquation(new SNREquation(0.3662, -0.9520));
        allMotes[24 - 2].getLinkTo(allMotes[21 - 2]).setSnrEquation(new SNREquation(0.5877, -4.4264));
        allMotes[25 - 2].getLinkTo(allMotes[10 - 2]).setSnrEquation(new SNREquation(0.5531, 6.5996));
        allMotes[26 - 2].getLinkTo(allMotes[15 - 2]).setSnrEquation(new SNREquation(0.1958, -5.6936));
        allMotes[27 - 2].getLinkTo(allMotes[28 - 2]).setSnrEquation(new SNREquation(0.2993, 2.0252));
        allMotes[28 - 2].getLinkTo(allMotes[20 - 2]).setSnrEquation(new SNREquation(0.6760, 4.9590));
        allMotes[29 - 2].getLinkTo(allMotes[20 - 2]).setSnrEquation(new SNREquation(0.4258, -3.9851));
        allMotes[30 - 2].getLinkTo(allMotes[31 - 2]).setSnrEquation(new SNREquation(0.8634, -6.5998));
        allMotes[31 - 2].getLinkTo(gateway).setSnrEquation(new SNREquation(0.4025, -2.8609));
        allMotes[32 - 2].getLinkTo(allMotes[31 - 2]).setSnrEquation(new SNREquation(0.8601, -7.9440));
        allMotes[33 - 2].getLinkTo(allMotes[29 - 2]).setSnrEquation(new SNREquation(0.6674, -7.8050));
        allMotes[34 - 2].getLinkTo(allMotes[33 - 2]).setSnrEquation(new SNREquation(0.1744, -4.9619));
        allMotes[35 - 2].getLinkTo(allMotes[27 - 2]).setSnrEquation(new SNREquation(0.1798, 4.1792));
        allMotes[35 - 2].getLinkTo(allMotes[30 - 2]).setSnrEquation(new SNREquation(0.2006, -5.2376));
        allMotes[36 - 2].getLinkTo(allMotes[32 - 2]).setSnrEquation(new SNREquation(0.3771, -6.4021));
        allMotes[37 - 2].getLinkTo(allMotes[32 - 2]).setSnrEquation(new SNREquation(0.2905, -1.0953));


        simul.getRunInfo().setGlobalInterference(new DoubleRange(3.0, 6.0));
        simul.setMaxTimeSlots(maxQueueSlots);
        return simul;
    }

    public static deltaiot.services.Mote getAfMote(Mote mote, Simulator simul) {
        int moteid = mote.getId();
        int load = mote.getLoad();
        double battery = mote.getBatteryRemaining();
        int parents = mote.getLinks().size();
        int dataProbability = (int) Math
                .round(mote.getActivationProbability().get(simul.getRunInfo().getRunNumber()) * 100);
        int keepAliveTime = mote.getKeepAliveTime();
        int MaxQueueSize = mote.getMaxQueueSize();
        int queueSize = mote.getQueueSize();
        int queueLoss = mote.getQueueLoss();
        List<deltaiot.services.Link> afLinks = new LinkedList<>();
        for (Link link : mote.getLinks()) {
            double latency = 0; // unused
            int power = link.getPowerNumber();
            int packetLoss = link.calculatePacketLoss(simul.getRunInfo());
            int source = link.getFrom().getId();
            int dest = link.getTo().getId();
            double sNR = link.getSNR(simul.getRunInfo());// getSnrEquation().getSNR(link.getPowerNumber());
            int distribution = link.getDistribution();
            int sF = link.getSfTimeNumber();

            deltaiot.services.Link afLink = new deltaiot.services.Link(latency, power, packetLoss, source, dest, sNR,
                    distribution, sF);
            afLinks.add(afLink);
        }

        deltaiot.services.Mote afMote = new deltaiot.services.Mote(moteid, load, battery, parents, dataProbability,
                queueSize, queueLoss, keepAliveTime, MaxQueueSize, afLinks);
        return afMote;
    }

    public static Simulator createSimulatorForTraining(int[] distributions, int[] powers, double globalInterference,
                                                       int probability1, int probability2) {
        Simulator simul = new Simulator();

        // Motes
        int load = 100;
        double battery = 11880.0;
        int maxQueueSize = 600;
        int keepAliveTime = 1;
        int maxQueueSlots = 400;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        Mote mote6 = new Mote(6, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote7 = new Mote(7, battery, load, maxQueueSize, keepAliveTime,
                new Position(324 * posScale, 273 * posScale), false);
        Mote mote8 = new Mote(8, battery, load, maxQueueSize, keepAliveTime,
                new Position(392 * posScale, 478 * posScale), true);
        Mote mote9 = new Mote(9, battery, load, maxQueueSize, keepAliveTime,
                new Position(540 * posScale, 479 * posScale), false);
        Mote mote10 = new Mote(10, battery, load, maxQueueSize, keepAliveTime,
                new Position(694 * posScale, 356 * posScale), true);
        Mote mote11 = new Mote(11, battery, load, maxQueueSize, keepAliveTime,
                new Position(234 * posScale, 232 * posScale), false);
        Mote mote12 = new Mote(12, battery, load, maxQueueSize, keepAliveTime,
                new Position(221 * posScale, 322 * posScale), false);
        Mote mote13 = new Mote(13, battery, load, maxQueueSize, keepAliveTime,
                new Position(142 * posScale, 170 * posScale), true);
        Mote mote14 = new Mote(14, battery, load, maxQueueSize, keepAliveTime,
                new Position(139 * posScale, 293 * posScale), true);
        Mote mote15 = new Mote(15, battery, load, maxQueueSize, keepAliveTime,
                new Position(128 * posScale, 344 * posScale), true);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7, mote8, mote9, mote10, mote11, mote12,
                mote13, mote14, mote15};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        SNREquation[] snrEquations = new SNREquation[]{new SNREquation(0.0169, 7.4076),
                new SNREquation(0.4982, 1.2468), new SNREquation(0.8282, -8.1246), new SNREquation(0.4932, -4.4898),
                new SNREquation(0.6199, -9.8051), new SNREquation(0.5855, -6.644), new SNREquation(0.5398, -2.0549),
                new SNREquation(0.5298, -0.1031), new SNREquation(0.8284, -7.2893), new SNREquation(0.8219, -7.3331),
                new SNREquation(0.6463, -3.0037), new SNREquation(0.714, -3.1985), new SNREquation(0.9254, -16.21),
                new SNREquation(0.1, 6), new SNREquation(0.6078, -3.6005), new SNREquation(0.4886, -4.7704),
                new SNREquation(0.5899, -7.1896)};

        for (int i = 0; i < 17; i++) {
            SNREquation eq = snrEquations[i];
            for (int j = 0; j <= 15; j++) {
                double p = eq.getSNR(j);
                if (p - globalInterference >= 0) {
                    powers[i] = j;
                    break;
                } else {
                    powers[i] = j;
                }
            }
        }

        // Links
        // int power = 15;
        int distribution = 100;
        boolean packetDuplication = false;
        simul.setPacketDuplication(packetDuplication);
        mote2.addLinkTo(mote4, gateway, powers[0], distribution);
        mote3.addLinkTo(gateway, gateway, powers[1], distribution);
        mote4.addLinkTo(gateway, gateway, powers[2], distribution);
        mote5.addLinkTo(mote9, gateway, powers[3], distribution);
        mote6.addLinkTo(mote4, gateway, powers[4], distribution);
        mote7.addLinkTo(mote2, gateway, powers[5], distributions[0]);
        mote7.addLinkTo(mote3, gateway, powers[6], distributions[1]);
        mote8.addLinkTo(gateway, gateway, powers[7], distribution);
        mote9.addLinkTo(gateway, gateway, powers[8], distribution);
        mote10.addLinkTo(mote6, gateway, powers[9], distributions[2]);
        mote10.addLinkTo(mote5, gateway, powers[10], distributions[3]);
        mote11.addLinkTo(mote7, gateway, powers[11], distribution);
        mote12.addLinkTo(mote7, gateway, powers[12], distributions[4]);
        mote12.addLinkTo(mote3, gateway, powers[13], distributions[5]);
        mote13.addLinkTo(mote11, gateway, powers[14], distribution);
        mote14.addLinkTo(mote12, gateway, powers[15], distribution);
        mote15.addLinkTo(mote12, gateway, powers[16], distribution);

        // Set order
        simul.setTurnOrder(8, 10, 13, 14, 15, 5, 6, 11, 12, 9, 7, 2, 3, 4);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 5,7,11,12 RFID sensors
        // 2,4,8,10,13,14 Passive infrared sensors
        mote2.setActivationProbability(new Constant<>(0.50));
        mote4.setActivationProbability(new Constant<>(0.50));
        mote5.setActivationProbability(new Constant<>(0.50));
        mote6.setActivationProbability(new Constant<>(0.50));
        mote7.setActivationProbability(new Constant<>(0.50));
        mote11.setActivationProbability(new Constant<>(0.50));
        mote12.setActivationProbability(new Constant<>(0.50));
        mote14.setActivationProbability(new Constant<>(0.50));

        mote10.setActivationProbability(new Constant<>(new Double(probability1) / 100)); // 0...1 double value
        mote13.setActivationProbability(new Constant<>(new Double(probability2) / 100)); // 0...1 double value

        // mote12.getLinkTo(mote3).setInterference(new
        // FileProfile("deltaiot/scenario_data/SNR2.txt", 0.0));
        // mote4.getLinkTo(gateway).setInterference(new DoubleRange(0.0, 25.0));

        // mote10.getLinkTo(mote6).setInterference(new
        // FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));

        // Add SNR equations new settings

        mote2.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0169, 7.4076));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.4982, 1.2468));
        mote4.getLinkTo(gateway).setSnrEquation(new SNREquation(0.8282, -8.1246));
        mote5.getLinkTo(mote9).setSnrEquation(new SNREquation(0.4932, -4.4898));
        mote6.getLinkTo(mote4).setSnrEquation(new SNREquation(0.6199, -9.8051));
        mote7.getLinkTo(mote3).setSnrEquation(new SNREquation(0.5855, -6.644));
        mote7.getLinkTo(mote2).setSnrEquation(new SNREquation(0.5398, -2.0549));
        mote8.getLinkTo(gateway).setSnrEquation(new SNREquation(0.5298, -0.1031));
        mote9.getLinkTo(gateway).setSnrEquation(new SNREquation(0.8284, -7.2893));
        mote10.getLinkTo(mote6).setSnrEquation(new SNREquation(0.8219, -7.3331));
        mote10.getLinkTo(mote5).setSnrEquation(new SNREquation(0.6463, -3.0037));
        mote11.getLinkTo(mote7).setSnrEquation(new SNREquation(0.714, -3.1985));
        mote12.getLinkTo(mote7).setSnrEquation(new SNREquation(0.9254, -16.21));
        mote12.getLinkTo(mote3).setSnrEquation(new SNREquation(0.1, 6));
        mote13.getLinkTo(mote11).setSnrEquation(new SNREquation(0.6078, -3.6005));
        mote14.getLinkTo(mote12).setSnrEquation(new SNREquation(0.4886, -4.7704));
        mote15.getLinkTo(mote12).setSnrEquation(new SNREquation(0.5899, -7.1896));

        // Add SNR equations (from Usman's settings class)
        // mote2 .getLinkTo(mote4 ).setSnrEquation(new SNREquation(
        // 0.0473684210526, -5.29473684211));
        // mote3 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.0280701754386, 4.25263157895));
        // mote4 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.119298245614, -1.49473684211));
        // mote5 .getLinkTo(mote9 ).setSnrEquation(new
        // SNREquation(-0.019298245614, 4.8));
        // mote6 .getLinkTo(mote4 ).setSnrEquation(new SNREquation(
        // 0.0175438596491, -3.84210526316));
        // mote7 .getLinkTo(mote3 ).setSnrEquation(new SNREquation(
        // 0.168421052632, 2.30526315789));
        // mote7 .getLinkTo(mote2 ).setSnrEquation(new
        // SNREquation(-0.0157894736842, 3.77894736842));
        // mote8 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.00350877192982, 0.45263157895));
        // mote9 .getLinkTo(gateway).setSnrEquation(new SNREquation(
        // 0.0701754385965, 2.89473684211));
        // mote10.getLinkTo(mote6 ).setSnrEquation(new SNREquation(
        // 3.51139336547e-16, -2.21052631579));
        // mote10.getLinkTo(mote5 ).setSnrEquation(new SNREquation(
        // 0.250877192982, -1.75789473684));
        // mote11.getLinkTo(mote7 ).setSnrEquation(new SNREquation(
        // 0.380701754386, -2.12631578947));
        // mote12.getLinkTo(mote7 ).setSnrEquation(new SNREquation(
        // 0.317543859649, 2.95789473684));
        // mote12.getLinkTo(mote3 ).setSnrEquation(new
        // SNREquation(0.0157894736842, -3.77894736842));
        // mote13.getLinkTo(mote11 ).setSnrEquation(new
        // SNREquation(0.0210526315789, -2.81052631579));
        // mote14.getLinkTo(mote12 ).setSnrEquation(new SNREquation(
        // 0.0333333333333, 2.58947368421));
        // mote15.getLinkTo(mote12 ).setSnrEquation(new SNREquation(
        // 0.0438596491228, 1.31578947368));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new Constant<>(globalInterference));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    public static Simulator createSimulatorForDeltaIoT5Motes() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 30;
        int keepAliveTime = 1;
        int maxQueueSlots = 30;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        // Mote mote6 = new Mote(6 , battery, load, maxQueueSize, keepAliveTime,
        // new Position(628 * posScale, 309 * posScale), false);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = true;
        mote2.addLinkTo(gateway, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(mote2, gateway, power, distribution);
        mote5.addLinkTo(mote3, gateway, power, distribution);
        mote5.addLinkTo(mote4, gateway, power, distribution);
        // mote6. addLinkTo(mote5, gateway, power, distribution);

        // Set order
        simul.setTurnOrder(5, 4, 3, 2);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 5,7,11,12 RFID sensors
        // 2,4,8,10,13,14 Passive infrared sensors
        mote4.setActivationProbability(new Constant<>(0.50));
        mote5.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));

        mote5.getLinkTo(mote4).setInterference(new FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));

        // Add SNR equations new settings
        mote2.getLinkTo(gateway).setSnrEquation(new SNREquation(0.20, -5.00));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.0280701754386, 4.25263157895));
        mote4.getLinkTo(mote2).setSnrEquation(new SNREquation(0.50, 2.0));
        mote5.getLinkTo(mote3).setSnrEquation(new SNREquation(-0.019298245614, 4.8));
        mote5.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0175438596491, -3.84210526316));
        // mote6 .getLinkTo(mote5 ).setSnrEquation(new SNREquation(
        // 0.168421052632, 2.30526315789));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new DoubleRange(-5.0, 5.0));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    public static Simulator createSimulatorForDeltaIoT6Motes() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 30;
        int keepAliveTime = 1;
        int maxQueueSlots = 30;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        Mote mote6 = new Mote(6, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote7 = new Mote(7, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = true;
        mote2.addLinkTo(gateway, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(mote2, gateway, power, distribution);
        mote5.addLinkTo(mote3, gateway, power, distribution);
        mote6.addLinkTo(mote4, gateway, power, distribution);
        mote6.addLinkTo(mote5, gateway, power, distribution);
        mote7.addLinkTo(gateway, gateway, power, distribution);

        // Set order
        simul.setTurnOrder(7, 6, 5, 4, 3, 2, 1);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 5,7,11,12 RFID sensors
        // 2,4,8,10,13,14 Passive infrared sensors
        mote4.setActivationProbability(new Constant<>(0.50));
        mote2.setActivationProbability(new Constant<>(0.50));
        mote6.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));

        mote6.getLinkTo(mote6).setInterference(new FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));

        // Add SNR equations new settings
        mote2.getLinkTo(gateway).setSnrEquation(new SNREquation(0.20, -5.00));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.0280701754386, 4.25263157895));
        mote4.getLinkTo(mote2).setSnrEquation(new SNREquation(0.50, 2.0));
        mote5.getLinkTo(mote3).setSnrEquation(new SNREquation(-0.019298245614, 4.8));
        mote6.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0175438596491, -3.84210526316));
        mote6.getLinkTo(mote5).setSnrEquation(new SNREquation(0.168421052632, 2.30526315789));
        mote7.getLinkTo(gateway).setSnrEquation(new SNREquation(1.0, 5.00));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new DoubleRange(-5.0, 5.0));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    public static Simulator createSimulatorForDeltaIoT7Motes() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 30;
        int keepAliveTime = 1;
        int maxQueueSlots = 30;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        Mote mote6 = new Mote(6, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote7 = new Mote(7, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote8 = new Mote(8, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7, mote8};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = true;
        mote2.addLinkTo(gateway, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(mote2, gateway, power, distribution);
        mote5.addLinkTo(mote3, gateway, power, distribution);
        mote6.addLinkTo(mote4, gateway, power, distribution);
        mote6.addLinkTo(mote5, gateway, power, distribution);
        mote7.addLinkTo(gateway, gateway, power, distribution);
        mote8.addLinkTo(mote2, gateway, power, distribution);
        // Set order
        simul.setTurnOrder(7, 6, 5, 4, 3, 8, 2, 1);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 5,7,11,12 RFID sensors
        // 2,4,8,10,13,14 Passive infrared sensors
        mote4.setActivationProbability(new Constant<>(0.50));
        mote2.setActivationProbability(new Constant<>(0.50));
        mote6.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));

        mote6.getLinkTo(mote6).setInterference(new FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));

        // Add SNR equations new settings
        mote2.getLinkTo(gateway).setSnrEquation(new SNREquation(0.20, -5.00));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.0280701754386, 4.25263157895));
        mote4.getLinkTo(mote2).setSnrEquation(new SNREquation(0.50, 2.0));
        mote5.getLinkTo(mote3).setSnrEquation(new SNREquation(-0.019298245614, 4.8));
        mote6.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0175438596491, -3.84210526316));
        mote6.getLinkTo(mote5).setSnrEquation(new SNREquation(0.168421052632, 2.30526315789));
        mote7.getLinkTo(gateway).setSnrEquation(new SNREquation(1.0, 5.00));
        mote8.getLinkTo(mote2).setSnrEquation(new SNREquation(0.00350877192982, 0.45263157895));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new DoubleRange(-5.0, 5.0));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    public static Simulator createSimulatorForDeltaIoT8Motes() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 30;
        int keepAliveTime = 1;
        int maxQueueSlots = 30;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        Mote mote6 = new Mote(6, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote7 = new Mote(7, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote8 = new Mote(8, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote9 = new Mote(9, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7, mote8, mote9};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = true;
        mote2.addLinkTo(gateway, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(mote2, gateway, power, distribution);
        mote5.addLinkTo(mote3, gateway, power, distribution);
        mote6.addLinkTo(mote4, gateway, power, distribution);
        mote6.addLinkTo(mote5, gateway, power, distribution);
        mote7.addLinkTo(gateway, gateway, power, distribution);
        mote8.addLinkTo(mote2, gateway, power, distribution);
        mote9.addLinkTo(gateway, gateway, power, distribution);

        // Set order
        simul.setTurnOrder(7, 6, 5, 4, 3, 8, 9, 2, 1);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 5,7,11,12 RFID sensors
        // 2,4,8,10,13,14 Passive infrared sensors
        mote4.setActivationProbability(new Constant<>(0.50));
        mote2.setActivationProbability(new Constant<>(0.50));
        mote6.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));

        mote6.getLinkTo(mote6).setInterference(new FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));

        // Add SNR equations new settings
        mote2.getLinkTo(gateway).setSnrEquation(new SNREquation(0.20, -5.00));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.0280701754386, 4.25263157895));
        mote4.getLinkTo(mote2).setSnrEquation(new SNREquation(0.50, 2.0));
        mote5.getLinkTo(mote3).setSnrEquation(new SNREquation(-0.019298245614, 4.8));
        mote6.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0175438596491, -3.84210526316));
        mote6.getLinkTo(mote5).setSnrEquation(new SNREquation(0.168421052632, 2.30526315789));
        mote7.getLinkTo(gateway).setSnrEquation(new SNREquation(1.0, 5.00));
        mote8.getLinkTo(mote2).setSnrEquation(new SNREquation(0.00350877192982, 0.45263157895));
        mote9.getLinkTo(gateway).setSnrEquation(new SNREquation(0.20, 5.20));
        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new DoubleRange(-5.0, 5.0));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    public static Simulator createSimulatorForDeltaIoT9() {
        Simulator simul = new Simulator();

        // Motes
        int load = 10;
        double battery = 11880.0;
        int maxQueueSize = 30;
        int keepAliveTime = 1;
        int maxQueueSlots = 30;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, maxQueueSize, keepAliveTime,
                new Position(378 * posScale, 193 * posScale), false);
        Mote mote3 = new Mote(3, battery, load, maxQueueSize, keepAliveTime,
                new Position(365 * posScale, 343 * posScale), false);
        Mote mote4 = new Mote(4, battery, load, maxQueueSize, keepAliveTime,
                new Position(508 * posScale, 296 * posScale), false);
        Mote mote5 = new Mote(5, battery, load, maxQueueSize, keepAliveTime,
                new Position(603 * posScale, 440 * posScale), false);
        Mote mote6 = new Mote(6, battery, load, maxQueueSize, keepAliveTime,
                new Position(628 * posScale, 309 * posScale), false);
        Mote mote7 = new Mote(7, battery, load, maxQueueSize, keepAliveTime,
                new Position(324 * posScale, 273 * posScale), false);
        Mote mote8 = new Mote(8, battery, load, maxQueueSize, keepAliveTime,
                new Position(392 * posScale, 478 * posScale), true);
        Mote mote9 = new Mote(9, battery, load, maxQueueSize, keepAliveTime,
                new Position(540 * posScale, 479 * posScale), false);
        Mote mote10 = new Mote(10, battery, load, maxQueueSize, keepAliveTime,
                new Position(694 * posScale, 356 * posScale), true);

        Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7, mote8, mote9, mote10};
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        boolean packetDuplication = true;
        mote2.addLinkTo(mote4, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(gateway, gateway, power, distribution);
        mote5.addLinkTo(mote9, gateway, power, distribution);
        mote6.addLinkTo(mote4, gateway, power, distribution);
        mote7.addLinkTo(mote2, gateway, power, packetDuplication ? 100 : 50);
        mote7.addLinkTo(mote3, gateway, power, packetDuplication ? 100 : 50);
        mote8.addLinkTo(gateway, gateway, power, distribution);
        mote9.addLinkTo(gateway, gateway, power, distribution);
        mote10.addLinkTo(mote6, gateway, power, packetDuplication ? 100 : 50);
        mote10.addLinkTo(mote5, gateway, power, packetDuplication ? 100 : 50);

        // Set order
        simul.setTurnOrder(8, 10, 5, 6, 9, 7, 2, 3, 4, 1);

        // Set profiles for some links and motes
        // 3,6,9,15 are heat sensors so these motes sends traffic all the time
        // 5,7,11,12 RFID sensors
        // 2,4,8,10,13,14 Passive infrared sensors
        mote5.setActivationProbability(new Constant<>(0.50));
        mote7.setActivationProbability(new Constant<>(0.50));

        mote2.setActivationProbability(new Constant<>(0.50));
        mote4.setActivationProbability(new Constant<>(0.50));
        mote6.setActivationProbability(new Constant<>(0.50));
        mote10.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));

        mote10.getLinkTo(mote6).setInterference(new FileProfile("deltaiot/scenario_data/SNR1.txt", 0.0));

        // Add SNR equations new settings
        mote2.getLinkTo(mote4).setSnrEquation(new SNREquation(0.20, -5.00));
        mote3.getLinkTo(gateway).setSnrEquation(new SNREquation(0.0280701754386, 4.25263157895));
        mote4.getLinkTo(gateway).setSnrEquation(new SNREquation(0.50, 2.0));
        mote5.getLinkTo(mote9).setSnrEquation(new SNREquation(-0.019298245614, 4.8));
        mote6.getLinkTo(mote4).setSnrEquation(new SNREquation(0.0175438596491, -3.84210526316));
        mote7.getLinkTo(mote3).setSnrEquation(new SNREquation(0.168421052632, 2.30526315789));
        mote7.getLinkTo(mote2).setSnrEquation(new SNREquation(1.0, 5.00));
        mote8.getLinkTo(gateway).setSnrEquation(new SNREquation(0.00350877192982, 0.45263157895));
        mote9.getLinkTo(gateway).setSnrEquation(new SNREquation(0.20, 5.20));
        mote10.getLinkTo(mote6).setSnrEquation(new SNREquation(3.51, -2.21052631579));
        mote10.getLinkTo(mote5).setSnrEquation(new SNREquation(0.250877192982, -1.75789473684));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo().setGlobalInterference(new DoubleRange(-5.0, 5.0));
        simul.setMaxTimeSlots(maxQueueSlots);

        return simul;
    }

    int getPower() {
        return 1;
    }

    public void run() {
        try {
            System.out.println("--START--");

            // Create socket
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting to connect...");
            Socket socket = serverSocket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream output = new PrintStream(socket.getOutputStream());
            System.out.println("Connected.");

            // Create thread that listens for messages from the client
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String fromFeedbackLoop = null;
                        while ((fromFeedbackLoop = input.readLine()) != null) {
                            applyNewSettings(fromFeedbackLoop);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }).start();

            // Do logic
            System.out.println("--SIMULATION--");
            List<String> printsBackup = new ArrayList<>();
            for (int i = 0; i < 96; ++i) {
                System.out.println("Run... " + i);

                // Do emulation
                simul.doSingleRun();
                String print = simul.getGatewayWithId(GATEWAY_ID).toString();
                printsBackup.add(print);
                System.out.println(print);

                // Send info to client
                String infoToSend = createInfoToSend();
                System.out.println("To FeedbackLoop: " + infoToSend);
                output.println(infoToSend);

                // Wait for client response
                lock.lock();
                try {
                    adaptationCompleted.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }

            // Report
            System.out.println("--FINAL REPORT--");
            for (String print : printsBackup) {
                System.out.println(print);
            }

            // Cleanup
            input.close();
            output.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String createInfoToSend() {
        deltaiot.DeltaIoT deltaIoT = new deltaiot.DeltaIoT();

        Gateway gw = simul.getGatewayWithId(GATEWAY_ID);
        List<Packet> queuedPackets = new LinkedList<>();
        for (Mote mote : gw.getView()) {
            queuedPackets.addAll(mote.getPacketQueue());
        }
        // deltaIoT.setPacketLoss(gw.calculatePacketLoss(runInfo));
        // deltaIoT.setPacketLoss(gw.calculatePacketLoss(queuedPackets));
        deltaIoT.setPower(gw.getPowerConsumed());

        HashMap<Integer, deltaiot.services.Mote> afMotes = new HashMap<>();
        for (Mote mote : gw.getView()) {
            int moteid = mote.getId();
            deltaiot.services.Mote afMote = getAfMote(mote, simul);
            afMotes.put(moteid, afMote);
        }
        deltaIoT.Motes = afMotes;

        return deltaIoT.toJson();
    }

    public void applyNewSettings(String msgSettings) {
        System.out.println("Message Received from Client:" + msgSettings);
        if (msgSettings.equalsIgnoreCase("NoAdaptationRequired")
                || msgSettings.equalsIgnoreCase("AdaptationCompleted")) {
            lock.lock();
            try {

                synchronized (adaptationCompleted) {
                    adaptationCompleted.signal();
                }
            } finally {
                lock.unlock();
            }
        } else {
            Gson gson = new Gson();
            deltaiot.services.Mote afMote = gson.fromJson(msgSettings, new TypeToken<deltaiot.services.Mote>() {
            }.getType());
            for (deltaiot.services.Link afLink : afMote.getLinks()) {

                Mote source = simul.getMoteWithId(afMote.getMoteid());
                Node destination = simul.getNodeWithId(afLink.getDest());
                Link actualLink = source.getLinkTo(destination);

                if (actualLink.getDistribution() != afLink.getDistribution()) {
                    actualLink.setDistribution(afLink.getDistribution());
                    System.out.println("Link distribution adapted: " + actualLink);
                }
                if (actualLink.getPowerNumber() != afLink.getPower()) {
                    actualLink.setPowerNumber(afLink.getPower());
                    System.out.println("Link power adapted:        " + actualLink);
                }
            }
        }
    }
}
