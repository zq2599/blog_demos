package com.bolingcavalry.simple.mapper;

import com.bolingcavalry.simple.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:32
 */

@Repository
public interface UserMapper {
    User sel(int id);
}
