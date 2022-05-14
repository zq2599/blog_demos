package com.bolingcavalry.multidb.service;


import com.bolingcavalry.multidb.entity.seconddb.Buyer;
import io.quarkus.hibernate.orm.PersistenceUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author will
 */
@ApplicationScoped
public class BuyerService {
    @Inject
    @PersistenceUnit("second_db")
    EntityManager entityManager;

    public List<Buyer> get() {
        return entityManager.createNamedQuery("Buyer.findAll", Buyer.class)
                .getResultList();
    }

    public Buyer getSingle(Integer id) {
        return entityManager.find(Buyer.class, id);
    }

    @Transactional
    public void create(Buyer buyer) {
        entityManager.persist(buyer);
    }

    @Transactional
    public void update(Integer id, Buyer buyer) {
        Buyer entity = entityManager.find(Buyer.class, id);

        if (null!=entity) {
            entity.setName(buyer.getName());
        }
    }

    @Transactional
    public void delete(Integer id) {
        Buyer entity = entityManager.getReference(Buyer.class, id);

        if (null!=entity) {
            entityManager.remove(entity);
        }
    }
}