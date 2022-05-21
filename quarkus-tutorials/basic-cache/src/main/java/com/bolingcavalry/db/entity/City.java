package com.bolingcavalry.db.entity;

import javax.persistence.*;

@Entity
@Table(name = "city")
@NamedQuery(name = "City.findAll", query = "SELECT c FROM City c ORDER BY c.name", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Cacheable
public class City {

    @Id
    @SequenceGenerator(name = "citySequence", sequenceName = "city_id_seq", allocationSize = 1, initialValue = 10)
    @GeneratedValue(generator = "citySequence")
    private Integer id;

    @Column(length = 40, unique = true)
    private String name;

    public City() {
    }

    public City(String name) {
        this.name = name;
    }

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

}