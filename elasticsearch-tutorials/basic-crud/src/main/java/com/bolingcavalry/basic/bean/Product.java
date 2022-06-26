package com.bolingcavalry.basic.bean;

import lombok.*;

/**
 * @program: elasticsearch-tutorials
 * @description: 产品的bean
 * @author: za2599@gmail.com
 * @create: 2022-06-26 11:14
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Product {
    private String id;
    private String name;
    private String description;
    private int price;
}
