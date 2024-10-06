package org.example.basket;

public interface BasketRepository {

    default Basket findById(BasketId basketId){
        EventStream.History history = findHistoryForBasketId(basketId);
        return Basket.replay(basketId, history);
    }

    EventStream.History findHistoryForBasketId(BasketId basketId);

    default void save(Basket basket) {
        save(basket.getId(), basket.getPendingEvents());
    }

    void save(BasketId basketId, EventStream.Pending pendingEvents);
}
