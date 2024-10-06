package org.example.basket;

import java.io.Serializable;

public sealed interface BasketEvent extends Serializable permits ItemAdded, QuantityChanged {

    BasketId basketId();

}
