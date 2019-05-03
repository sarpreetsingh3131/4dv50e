package mapek;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Goal {

    private static Set<String> ALLOWED_OPERATORS = new HashSet<>(Arrays.asList("<", ">", "<=", ">=", "==", "!="));
    private String target;
    private String operator;
    private double tresshold;

    public Goal(String target, String operator, Double tresshold) throws IllegalArgumentException {
        if (ALLOWED_OPERATORS.stream().noneMatch(op -> op.equals(operator))) {
            throw new IllegalArgumentException("Unaccepted operator '" + operator + "'.\nSupported operators: "
                    + ALLOWED_OPERATORS.toString());
        }
        this.target = target;
        this.operator = operator;
        this.tresshold = tresshold;
    }

    public String getTarget() {
        return this.target;
    }

    public String getOperator() {
        return this.operator;
    }

    public double getTresshold() {
        return this.tresshold;
    }

    public boolean evaluate(double value) throws IllegalArgumentException {

        if ("<".equals(operator)) {
            return value < tresshold;
        } else if (">".equals(operator)) {
            return value > tresshold;
        } else if ("<=".equals(operator)) {
            return value <= tresshold;
        } else if (">=".equals(operator)) {
            return value >= tresshold;
        } else if ("==".equals(operator)) {
            return value == tresshold;
        } else if ("!=".equals(operator)) {
            return value != tresshold;
        } else {
            throw new IllegalArgumentException("Illegal operator.");
        }
    }
}