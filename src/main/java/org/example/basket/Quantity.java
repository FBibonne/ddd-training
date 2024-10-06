package org.example.basket;

import java.io.Serializable;

public interface Quantity extends Serializable {

    Quantity ZERO = new Zero();
    Quantity ONE = of(1);
    Quantity TEN = of(10);

    int value();

    default Quantity increase(Quantity quantity) {
        return of(value() + quantity.value());
    }

    default Quantity decrease(Quantity quantity) {
        int actual = value() - quantity.value();
        return actual < 0 ? ZERO : of(actual);
    }

    static Quantity of(int value) {
        return switch (value) {
            case 0 -> ZERO;
            default -> Positive.from(value);
        };
    }

    record Positive(int value) implements Quantity {

        public Positive {
            if (value <= 0) {
                throw new QuantityMustBeZeroOrPositive(value);
            }
        }

        public static Positive from(int value) {
            return new Positive(value);
        }

    }

    record Zero() implements Quantity {

        @Override
        public int value() {
            return 0;
        }

    }
}
