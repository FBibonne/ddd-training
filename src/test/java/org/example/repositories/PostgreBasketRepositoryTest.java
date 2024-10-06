package org.example.repositories;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.basket.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.InvalidClassException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.basket.BasketFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.util.SerializationUtils.serialize;

class PostgreBasketRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript(PostgreBasketRepository.INIT_SCRIPT);

    DataSource dataSource;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    @Test
    void testSaveAndFindById() {

        BasketRepository basketRepository = new PostgreBasketRepository(dataSource);

        Basket basket = Basket.empty(BASKET_ID);
        AddItem command = new AddItem(BASKET_ID, ITEM_A, Quantity.TEN);
        AddItem command2 = new AddItem(BASKET_ID, ITEM_B, Quantity.ONE);
        basket.accept(command);
        basket.accept(command2);

        basketRepository.save(basket);

        assertEquals(basket, basketRepository.findById(BASKET_ID));
    }

    @Test
    void testSecureDeserialize() {
        PostgreBasketRepository basketRepository = new PostgreBasketRepository(dataSource);
        int basketId = 1;
        PostgreBasketRepository.BasketForPersistance basketForPersistance = new PostgreBasketRepository.BasketForPersistance(
                basketId,
                serialize(new StringBuilder("Hacked"))
                );
        basketRepository.simpleJdbcInsert().executeBatch(new SimplePropertySqlParameterSource(basketForPersistance));
        byte[] bytes = basketRepository.findEventsByBasketId(BasketId.of(basketId));
        assertThatThrownBy(()->basketRepository.secureDeserialize(bytes)).isInstanceOf(IllegalArgumentException.class)
                .cause().isInstanceOf(InvalidClassException.class).message().contains("filter status: REJECTED");
    }


}
