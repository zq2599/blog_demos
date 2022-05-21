package com.bolingcavalry.multidb.entity.firstdb;

import javax.persistence.*;

@Entity
@Table(name = "seller")
@NamedQuery(name = "Seller.findAll", query = "SELECT f FROM Seller f ORDER BY f.name", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Cacheable
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(name = "product_num")
    private int productNum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductNum() {
        return productNum;
    }

    public void setProductNum(int productNum) {
        this.productNum = productNum;
    }
}