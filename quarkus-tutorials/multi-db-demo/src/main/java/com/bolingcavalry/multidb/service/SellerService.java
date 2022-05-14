package com.bolingcavalry.multidb.service;


import com.bolingcavalry.multidb.entity.firstdb.Seller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author will
 */
@ApplicationScoped
public class SellerService {
    @Inject
    EntityManager entityManager;

    public List<Seller> get() {
        return entityManager.createNamedQuery("Seller.findAll", Seller.class)
                .getResultList();
    }

    public Seller getSingle(Integer id) {
        return entityManager.find(Seller.class, id);
    }

    @Transactional
    public void create(Seller seller) {
        entityManager.persist(seller);
    }

    @Transactional
    public void update(Integer id, Seller seller) {
        Seller entity = entityManager.find(Seller.class, id);

        if (null!=entity) {
            entity.setName(seller.getName());
        }
    }

    @Transactional
    public void delete(Integer id) {
        Seller entity = entityManager.getReference(Seller.class, id);

        if (null!=entity) {
            entityManager.remove(entity);
        }
    }
}