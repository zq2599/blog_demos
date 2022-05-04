package com.bolingcavalry.db.service;


import com.bolingcavalry.db.entity.Fruit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author will
 */
@ApplicationScoped
public class FruitService {
    @Inject
    EntityManager entityManager;

    public List<Fruit> get() {
        return entityManager.createNamedQuery("Fruits.findAll", Fruit.class)
                .getResultList();
    }

    public Fruit getSingle(Integer id) {
        return entityManager.find(Fruit.class, id);
    }

    @Transactional
    public void create(Fruit fruit) {
        entityManager.persist(fruit);
    }

    @Transactional
    public void update(Integer id, Fruit fruit) {
        Fruit entity = entityManager.find(Fruit.class, id);

        if (null!=entity) {
            entity.setName(fruit.getName());
        }
    }

    @Transactional
    public void delete(Integer id) {
        Fruit entity = entityManager.getReference(Fruit.class, id);

        if (null!=entity) {
            entityManager.remove(entity);
        }
    }
}