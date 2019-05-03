package mapek;

// This class holds all the motes and general information (noise, topology, ...) of the network.
public class Configuration {

    ManagedSystem system;

    // The environment contains the SNR on a given link and the load.
    Environment environment;

    // An object holding single qualities.
    Qualities qualities;

    public Configuration() {
        system = new ManagedSystem();
        environment = new Environment();
        qualities = new Qualities();
    }

    protected Configuration getCopy() {
        Configuration newConfiguration = new Configuration();
        newConfiguration.system = system.getCopy();
        newConfiguration.environment = environment.getCopy();
        newConfiguration.qualities = qualities.getCopy();
        return newConfiguration;
    }
}
