package com.bolingcavalry.parameterized.service.impl;

import lombok.Data;

/**
 * @Description: 普通bean
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/5 0:02
 */
@Data
public class Person {
    private String firstName;
    private String lastName;
    private Types type;
}
