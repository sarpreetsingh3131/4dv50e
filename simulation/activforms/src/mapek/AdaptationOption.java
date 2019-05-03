package mapek;

import com.google.gson.Gson;

public class AdaptationOption {
    public ManagedSystem system;
    public Qualities verificationResults = new Qualities();

    // Index used to track the adaptation option (in the overall list of options)
    public int overallIndex;

    // Boolean which indicates if this option is verified
    public boolean isVerified = false;

    public AdaptationOption getCopy() {
        AdaptationOption newOption = new AdaptationOption();
        newOption.system = system.getCopy();
        newOption.verificationResults = verificationResults.getCopy();
        newOption.overallIndex = overallIndex;
        newOption.isVerified = isVerified;
        return newOption;
    }

    @Override
    public String toString() {
        Gson gsn = new Gson();
        return gsn.toJson(this);
    }

    public String toModelString() {
        StringBuilder string = new StringBuilder();
        string.append("\nManagedSystem deltaIoT = {{");
        Mote mote;
        for (int i : system.motes.keySet()) {
            mote = system.getMote(i);

            string.append(mote.getModelString());
            string.append(",");
        }

        string.setLength(string.length() - 1);
        string.append("\n}};");
        return string.toString();
    }
}
