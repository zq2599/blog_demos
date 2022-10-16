package com.bolingcavalry.repository;

import com.bolingcavalry.model.Person;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PersonRepositoryAsyncAwait {

    @Inject
    PgPool pgPool;

    public Person findById(Long id) {
        RowSet<Row> rowSet = pgPool
           .preparedQuery("SELECT id, name, age, gender, external_id FROM person WHERE id = $1")
           .executeAndAwait(Tuple.of(id));
        List<Person> persons = iterateAndCreate(rowSet);
        return persons.size() == 0 ? null : persons.get(0);
    }

    private List<Person> iterateAndCreate(RowSet<Row> rowSet) {
        List<Person> persons = new ArrayList<>();
        for (Row row : rowSet) {
            persons.add(Person.from(row));
        }
        return persons;
    }
}
