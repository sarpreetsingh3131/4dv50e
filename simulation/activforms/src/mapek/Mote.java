package mapek;

import java.util.LinkedList;
import java.util.List;

public class Mote {
    int moteId;
    double energyLevel;
    int load;
    int queueSize;

    List<Link> links = new LinkedList<>();

    public int getMoteId() {
        return moteId;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Link getLink(int index) {
        return links.get(index);
    }

    public double getEnergyLevel() {
        return energyLevel;
    }

    public Mote getCopy() {
        Mote mote = new Mote();
        mote.moteId = this.moteId;
        mote.energyLevel = this.energyLevel;
        mote.load = this.load;
        mote.queueSize = this.queueSize;

        for (Link link : links) {
            mote.links.add(link.getCopy());
        }
        return mote;
    }

    public String getModelString() {
        StringBuilder string = new StringBuilder();
        string.append("\n{");

        string.append(String.format("%d, %d, 11744, %d, %d,{", moteId, load, links.size(), queueSize));
        for (Link link : links) {
            string.append(
                    String.format("{%d, %d, %d, %d},", link.source, link.destination, link.power, link.distribution));
        }
        if (links.size() == 1) {
            // add empty link
            string.append("{0, 0, 0, 0}");
        } else {
            string.setLength(string.length() - 1);
        }

        string.append("}}");
        return string.toString();
    }
}
