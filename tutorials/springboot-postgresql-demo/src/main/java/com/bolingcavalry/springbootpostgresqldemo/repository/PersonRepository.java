package com.bolingcavalry.springbootpostgresqldemo.repository;

import com.bolingcavalry.springbootpostgresqldemo.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

}
