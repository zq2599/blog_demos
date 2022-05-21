package com.bolingcavalry.db.service;


import com.bolingcavalry.db.entity.City;
import com.bolingcavalry.db.entity.Country;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author will
 */
@ApplicationScoped
public class CityService {
    @Inject
    EntityManager entityManager;

    public City getSingle(Integer id) {
        return entityManager.find(City.class, id);
    }

    public List<City> get() {
        return entityManager.createNamedQuery("City.findAll", City.class)
                .getResultList();
    }

    @Transactional
    public void create(City fruit) {
        entityManager.persist(fruit);
    }

    @Transactional
    public void update(Integer id, City fruit) {
        City entity = entityManager.find(City.class, id);

        if (null!=entity) {
            entity.setName(fruit.getName());
        }
    }

    @Transactional
    public void delete(Integer id) {
        City entity = entityManager.getReference(City.class, id);

        if (null!=entity) {
            entityManager.remove(entity);
        }
    }
}