package org.example.basket;

import java.io.Serializable;

public record ItemAdded(BasketId basketId, ItemId itemId, Quantity quantity) implements BasketEvent, Serializable {
}
