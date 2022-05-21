package com.bolingcavalry.db.service;


import com.bolingcavalry.db.entity.City;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

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
}