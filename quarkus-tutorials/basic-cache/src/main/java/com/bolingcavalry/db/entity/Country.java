package com.bolingcavalry.db.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;
@Entity
@Table(name = "country")
@Cacheable
public class Country {

    @Id
    @SequenceGenerator(name = "countrySequence", sequenceName = "country_id_seq", allocationSize = 1, initialValue = 10)
    @GeneratedValue(generator = "countrySequence")
    private Integer id;

    @Column(length = 40, unique = true)
    private String name;

    @OneToMany
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    List<City> cities;

    public Country() {
    }

    public Country(String name) {
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

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

}