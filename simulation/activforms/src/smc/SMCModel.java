package smc;

public class SMCModel {
    String key;
    String path;
    ModelType type;
    int simulations;
    double alpha;
    double epsilon;
    String model;

    public SMCModel(String key, String path, ModelType type, int simulations, double alpha, double epsilon,
                    String model) {
        this.key = key;
        this.path = path;
        this.type = type;
        this.simulations = simulations;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.model = model;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public ModelType getType() {
        return type;
    }

    public int getSimulations() {
        return simulations;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return String.format("Key:%s, path:%s, type:%s, simulations:%s, alpha:%s, epsilon:%s, model:%s", key, path,
                type.name(), simulations, alpha, epsilon, model.substring(0, 5));
    }
}
