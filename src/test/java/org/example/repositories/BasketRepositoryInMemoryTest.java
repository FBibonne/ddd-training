package org.example.repositories;

import org.example.basket.AddItem;
import org.example.basket.Basket;
import org.example.basket.BasketRepository;
import org.example.basket.Quantity;
import org.junit.jupiter.api.Test;

import static org.example.basket.BasketFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BasketRepositoryInMemoryTest {

    @Test
    void testSaveAndFindById(){

        BasketRepository basketRepository = new InMemoryBasketRepository();

        Basket basket = Basket.empty(BASKET_ID);
        AddItem command = new AddItem(BASKET_ID, ITEM_A, Quantity.TEN);
        AddItem command2 = new AddItem(BASKET_ID, ITEM_B, Quantity.ONE);
        basket.accept(command);
        basket.accept(command2);

        basketRepository.save(basket);

        assertEquals(basket, basketRepository.findById(BASKET_ID));
    }

}
