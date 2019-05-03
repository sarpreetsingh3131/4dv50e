package mapek;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

// The environment class holds the uncertainties that impact the qualities of the system
public class Environment {

    public List<SNR> linksSNR = new LinkedList<>();
    public List<TrafficProbability> motesLoad = new LinkedList<>();

    public double getSNR(Link link) {
        for (SNR snr : linksSNR) {
            if (snr.source == link.getSource() && link.destination == link.getDestination()) {
                return snr.SNR;
            }
        }
        throw new RuntimeException("SNR not found of the given link:" + link);
    }

    public void setSNR(Link link, double newSNR) {
        for (SNR snr : linksSNR) {
            if (snr.source == link.getSource() && link.destination == link.getDestination()) {
                snr.SNR = newSNR;
                return;
            }
        }
        throw new RuntimeException("SNR not found of the given link:" + link);
    }

    // Returns a JSON string representing the object
    @Override
    public String toString() {
        Gson gsn = new Gson();
        return gsn.toJson(this);
    }

    public Environment getCopy() {
        Environment env = new Environment();
        for (SNR snr : linksSNR) {
            env.linksSNR.add(snr.getCopy());
        }

        for (TrafficProbability load : motesLoad) {
            env.motesLoad.add(load.getCopy());
        }
        return env;
    }

    /**
     * String that is used to adjust the configuration in the quality models.
     *
     * @return the string that will be put in the quality model.
     */
    public String toModelString() {
        StringBuilder string = new StringBuilder();

        string.append("Environment environment = {\n{");

        for (SNR snr : linksSNR) {
            string.append(String.format("{%d, %d, %d},", snr.source, snr.destination, Math.round(snr.SNR)));
        }

        string.setLength(string.length() - 1);

        string.append("},\n{");

        for (TrafficProbability load : motesLoad) {
            string.append(String.format("{%d, %d},", load.moteId, Math.round(load.load)));
        }

        string.setLength(string.length() - 1);

        string.append("}};");

        return string.toString();
    }
}
