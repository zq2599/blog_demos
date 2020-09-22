package com.bolingcavalry.druidtwosource.mapper.second;

import com.bolingcavalry.druidtwosource.entity.Address;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: 地址实体的接口类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:32
 */

@Repository
public interface AddressMapper {

    int insertWithFields(Address address);

    List<Address> findByCityName(String cityName);

    int delete(int id);

}
