package com.bolingcavalry.simple.service;

import com.bolingcavalry.simple.entity.User;
import com.bolingcavalry.simple.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public User sel(int id) {
        return userMapper.sel(id);
    }

}
