package com.bolingcavalry.druidtwosource.service;

import com.bolingcavalry.druidtwosource.entity.Address;
import com.bolingcavalry.druidtwosource.entity.User;
import com.bolingcavalry.druidtwosource.mapper.first.UserMapper;
import com.bolingcavalry.druidtwosource.mapper.second.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@Service
public class AddressService {

    @Autowired
    AddressMapper addressMapper;


    public Address insertWithFields(Address address) {
        addressMapper.insertWithFields(address);
        return address;
    }

    public List<Address> findByCityName(String cityName) {
        return addressMapper.findByCityName(cityName);
    }


    public int delete(int id) {
        return addressMapper.delete(id);
    }

}
