package com.bolingcavalry.druidonesource.mapper;

import com.bolingcavalry.druidonesource.entity.LogExtend;
import com.bolingcavalry.druidonesource.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:32
 */

@Repository
public interface UserMapper {

    User sel(int id);

    int insertWithFields(User user);

    int insertBatch(List<User> users);

    int clearAll();

    List<User> findByName(String name);

    int update(User user);

    int delete(int id);

    int totalCount();

    LogExtend selExtend(int id);
}
