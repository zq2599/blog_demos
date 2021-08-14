package com.bolingcavalry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Student {
    String name;
    int age;

    public static void test(){
        new Student("1",2);
    }
}