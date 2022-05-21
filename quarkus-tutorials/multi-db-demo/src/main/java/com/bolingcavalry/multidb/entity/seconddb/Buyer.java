package com.bolingcavalry.multidb.entity.seconddb;

import javax.persistence.*;

@Entity
@Table(name = "buyer")
@NamedQuery(name = "Buyer.findAll", query = "SELECT f FROM Buyer f ORDER BY f.name", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Cacheable
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(name = "order_num")
    private int orderNum;

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

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }
}