package org.example.repositories;

import org.example.basket.*;
import org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.Arrays;

import static org.springframework.util.SerializationUtils.serialize;

public record PostgreBasketRepository(SimpleJdbcInsert simpleJdbcInsert,
                                      JdbcClient jdbcClient) implements BasketRepository {

    public static final String INIT_SCRIPT = "initBasketRepository.sql";
    private static final String BASKET_EVENTS_TABLE = "basket_events";
    private static final String BASKET_ID_PARAM = "basketId";
    private static final String BASKET_EVENTS_SELECT = "select events_data from " + BASKET_EVENTS_TABLE + " where basket_id = :" + BASKET_ID_PARAM;
    private static final Class<?>[] authorizedClasses = {BasketEvent[].class, BasketEvent.class, BasketId.class, ItemId.class, Quantity.class};


    public PostgreBasketRepository(DataSource dataSource) {
        this(new SimpleJdbcInsert(dataSource).withTableName(BASKET_EVENTS_TABLE), JdbcClient.create(dataSource));
    }

    @Override
    public EventStream.History findHistoryForBasketId(BasketId basketId) {
        return EventStream.History.of(secureDeserialize(findEventsByBasketId(basketId)));
    }

    BasketEvent[] secureDeserialize(byte[] bytes) {

        if (bytes == null) {
            return new BasketEvent[0];
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            ois.setObjectInputFilter(this::basketEventArrayOnlySecureFilter);
            return (BasketEvent[]) ois.readObject();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to deserialize object", ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to deserialize object type", ex);
        }

    }

    private ObjectInputFilter.Status basketEventArrayOnlySecureFilter(ObjectInputFilter.FilterInfo filterInfo) {
        Class<?> clazz = filterInfo.serialClass();
        if (clazz == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        return Arrays.stream(authorizedClasses).anyMatch(c -> c.isAssignableFrom(clazz)) ?
                ObjectInputFilter.Status.ALLOWED :
                ObjectInputFilter.Status.REJECTED;
    }

    byte[] findEventsByBasketId(BasketId basketId) {
        return jdbcClient.sql(BASKET_EVENTS_SELECT)
                .param(BASKET_ID_PARAM, basketId.value())
                .query(byte[].class)
                .single();
    }

    @Override
    public void save(BasketId basketId, EventStream.Pending pendingEvents) {
        simpleJdbcInsert.executeBatch(toSqlParameterSource(basketId, pendingEvents.toArray()));
    }

    private SqlParameterSource toSqlParameterSource(BasketId basketId, BasketEvent[] events) {
        return new SimplePropertySqlParameterSource(new BasketForPersistance(basketId, events));
    }

    record BasketForPersistance(int basketId, byte[] eventsData) {
        public BasketForPersistance(BasketId basketId, BasketEvent[] events) {
            this(basketId.value(), serialize(events));
        }
    }
}
