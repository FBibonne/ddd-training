package org.example.basket;

import java.io.Serializable;

public record BasketId(int value) implements Serializable {

    public static BasketId of(int value) {
        return new BasketId(value);
    }

}
