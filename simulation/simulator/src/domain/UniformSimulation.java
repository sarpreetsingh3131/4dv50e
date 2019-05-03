package domain;

//import java.util.Random;

public class UniformSimulation {
    int unit = 100;
    int value = 40;
    int period;
    int remainder;
    // Random r = new Random(100);
    int counter = 0;
    int remainderCount;
    int count;

    public UniformSimulation(Double value, int unit) {
        this(((Double) (value * unit)).intValue(), unit);
    }

    public UniformSimulation(int value, int unit) {
        int gcd = gcd(value, unit);

        this.unit = unit / gcd;
        this.value = value / gcd;

        // No floats
        // if (unit % value == 0){
        if (this.value != 0) {
            period = this.unit / this.value;
            remainder = this.unit % this.value;
        } else {
            period = this.unit;
            remainder = 0;
        }
        // else{;

        // }

    }

    public static void main(String[] args) {
        UniformSimulation simulation = new UniformSimulation(10, 100);

        for (int i = 0; i < 100; i++) {
            System.out.println(simulation.simulate() == false ? 0 : 1);
        }
    }

    private static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    public boolean simulate() {
        if (this.value == 0)
            return true;
        counter++;
        remainderCount += remainder;

        boolean result = counter % period == 0;
        if (count == value)
            result = false;
        if (result) {
            count++;
        }

        if (counter == unit) {
            counter = 0;
            count = 0;
        }

        return result;
        // return counter % period == 0; //|| remainderCount % unit == 0;
    }
}
