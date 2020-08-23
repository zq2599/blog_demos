package com.bolingcavalry.druidtwosource.controller;

import com.bolingcavalry.druidtwosource.entity.Address;
import com.bolingcavalry.druidtwosource.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: user表操作的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/address")
@Api(tags = {"AddressController"})
public class AddressController {

    @Autowired
    private AddressService addressService;


    @ApiOperation(value = "新增address记录", notes="新增address记录")
    @RequestMapping(value = "/insertwithfields",method = RequestMethod.PUT)
    public Address create(@RequestBody Address address) {
        return addressService.insertWithFields(address);
    }

    @ApiOperation(value = "删除指定ID的address记录", notes="删除指定ID的address记录")
    @ApiImplicitParam(name = "id", value = "地址ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int delete(@PathVariable int id){
        return addressService.delete(id);
    }

    @ApiOperation(value = "根据城市名模糊查找所address记录", notes="根据城市名模糊查找所address记录")
    @ApiImplicitParam(name = "name", value = "城市名", paramType = "path", required = true, dataType = "String")
    @RequestMapping(value = "/findbycityname/{cityname}", method = RequestMethod.GET)
    public List<Address> findByName(@PathVariable("cityname") String cityName){
        return addressService.findByCityName(cityName);
    }

}
