package com.bolingcavalry.grpctutorials;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: TODO
 * @date 2021/4/20 8:32
 */
@Data
@AllArgsConstructor
public class DispOrder {
    private int orderId;
    private int productId;
    private String orderTime;
    private String buyerRemark;
}
