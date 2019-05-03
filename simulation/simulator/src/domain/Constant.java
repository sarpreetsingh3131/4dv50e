package domain;

public class Constant<T> implements Profile<T> {

    T value;

    public Constant(T value) {
        this.value = value;
    }

    public T get(int turnNumber) {
        return value;
    }
}
