package com.bolingcavalry.druidtwosource.mapper.first;

import com.bolingcavalry.druidtwosource.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:32
 */

@Repository
public interface UserMapper {

    int insertWithFields(User user);

    List<User> findByName(String name);

    int delete(int id);
}
