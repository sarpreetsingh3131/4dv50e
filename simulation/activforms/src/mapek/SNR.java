package mapek;

public class SNR {
    public int source;
    public int destination;
    public double SNR;

    public SNR(int source, int destination, double snr) {
        this.source = source;
        this.destination = destination;
        this.SNR = snr;
    }

    public SNR getCopy() {
        return new SNR(source, destination, SNR);
    }
}
