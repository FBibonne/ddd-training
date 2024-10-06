package org.example.basket;

import java.io.Serializable;

public record ItemId(int value) implements Serializable {

    public static ItemId of(int value) {
        return new ItemId(value);
    }

}
