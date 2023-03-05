package nextstep.subway.domain;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public enum Fare {
    BELOW_10KM(
        distance -> distance <= 10,
        distance -> 0),
    OVER_10KM_BELOW_50KM(
        distance -> 10 < distance && distance <= 50,
        distance -> calculateExtraFare(distance, Fare.UNIT_DISTANCE_OVER_10KM_BELOW_50KM)
    ),
    OVER_50KM(
        distance -> distance > 50,
        distance -> calculateExtraFare(distance, Fare.UNIT_DISTANCE_OVER_50KM)
    );

    private static final int BASE_FARE = 1250;
    private static final int BASE_DISTANCE = 10;
    private static final int UNIT_DISTANCE_OVER_10KM_BELOW_50KM = 5;
    private static final int UNIT_DISTANCE_OVER_50KM = 8;
    private static final int EXTRA_UNIT_FEE_PER_UNIT_DISTANCE = 100;

    private final Predicate<Integer> condition;
    private final UnaryOperator<Integer> operator;

    Fare(Predicate<Integer> condition, UnaryOperator<Integer> operator) {
        this.condition = condition;
        this.operator = operator;
    }

    public static int calculate(int distance) {
        return calculate(distance, 0);
    }

    public static int calculate(int distance, int lineExtraFare) {
        var fare = Arrays.stream(values())
            .filter(it -> satisfyCondition(it, distance))
            .findFirst()
            .orElse(BELOW_10KM);

        return BASE_FARE + calculateExtraFare(fare, distance) + lineExtraFare;
    }

    private static boolean satisfyCondition(Fare fare, int distance) {
        return fare.condition.test(distance);
    }

    private static int calculateExtraFare(Fare fare, int distance) {
        return fare.operator.apply(distance);
    }

    private static int calculateExtraFare(int distance, int unitDistance) {
        return (int)((Math.ceil((distance - BASE_DISTANCE - 1) / unitDistance) + 1) * EXTRA_UNIT_FEE_PER_UNIT_DISTANCE);
    }
}