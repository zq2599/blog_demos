package com.bolingcavalry.model;

import io.vertx.mutiny.sqlclient.Row;

public class Person {
    private Long id;
    private String name;
    private int age;
    private Gender gender;
    private Integer externalId;

    public String getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(String threadInfo) {
        this.threadInfo = threadInfo;
    }

    private String threadInfo;

    public Person() {
    }

    public Person(Long id, String name, int age, Gender gender, Integer externalId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.externalId = externalId;
        this.threadInfo = Thread.currentThread().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public static Person from(Row row) {
        return new Person(
                row.getLong("id"),
                row.getString("name"),
                row.getInteger("age"),
                Gender.valueOf(row.getString("gender")),
                row.getInteger("external_id"));
    }
}
