package org.example.repositories;

import org.example.basket.BasketEvent;
import org.example.basket.BasketId;
import org.example.basket.BasketRepository;
import org.example.basket.EventStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record InMemoryBasketRepository(Map<BasketId, List<BasketEvent>> events) implements BasketRepository {

    public InMemoryBasketRepository(){
        this(new HashMap<>());
    }

    @Override
    public EventStream.History findHistoryForBasketId(BasketId basketId) {
        return EventStream.History.of(events.getOrDefault(basketId,List.of()).toArray(BasketEvent[]::new));
    }

    @Override
    public void save(BasketId basketId, EventStream.Pending pendingEvents) {
        List<BasketEvent> basketEvents = new ArrayList<>();
        pendingEvents.forEach(basketEvents::add);
        events.put(basketId, basketEvents);
    }
}
