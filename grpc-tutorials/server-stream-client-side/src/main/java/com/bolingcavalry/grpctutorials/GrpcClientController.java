package com.bolingcavalry.grpctutorials;

import com.bolingcavalry.grpctutorials.lib.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: web接口类
 * @date 2021/4/17 10:01
 */
@RestController
public class GrpcClientController {

    @Autowired
    private GrpcClientService grpcClientService;

    @RequestMapping("/")
    public List<DispOrder> printMessage(@RequestParam(defaultValue = "will") String name) {
        return grpcClientService.listOrders(name);
    }
}
