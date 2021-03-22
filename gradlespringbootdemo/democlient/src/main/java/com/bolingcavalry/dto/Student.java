package com.bolingcavalry.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class Student {
    String name;
    int age;
}