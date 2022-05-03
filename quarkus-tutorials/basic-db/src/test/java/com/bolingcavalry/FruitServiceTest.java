package com.bolingcavalry;

import com.bolingcavalry.db.entity.Fruit;
import com.bolingcavalry.db.service.FruitService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class FruitServiceTest {

    @Inject
    FruitService fruitService;

    @Test
    public void testGet() {
        List<Fruit> list = fruitService.get();
        Assertions.assertNotNull(list);
        Assertions.assertFalse(list.isEmpty());
    }
}
