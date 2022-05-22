package com.bolingcavalry.db.service;


import com.bolingcavalry.db.entity.Country;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author will
 */
@ApplicationScoped
public class CountyService {
    @Inject
    EntityManager entityManager;

    public Country getSingle(Integer id) {
        return entityManager.find(Country.class, id);
    }
}