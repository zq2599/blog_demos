package com.bolingcavalry.springbootpostgresqldemo.web;

import com.bolingcavalry.springbootpostgresqldemo.model.Person;
import com.bolingcavalry.springbootpostgresqldemo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {

    @Autowired
    private PersonRepository personRepository;

    @RequestMapping(value = "/springboot/persons/{id}", method = RequestMethod.GET)
    public Person getPerson(@PathVariable("id") long id) {
        return personRepository.getReferenceById(id);
    }
}
