package com.bolingcavalry.curd.service;

import com.bolingcavalry.curd.entity.User;
import com.bolingcavalry.curd.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public User insertWithFields(User user) {
        userMapper.insertWithFields(user);
        return user;
    }

    public List<User> insertBatch(List<User> users) {
        userMapper.insertBatch(users);
        return users;
    }

    public int clearAll() {
       return userMapper.clearAll();
    }

    public List<User> findByName(String name) {
        return userMapper.findByName(name);
    }

    public int update(User user) {
        return userMapper.update(user);
    }

    public int delete(int id) {
        return userMapper.delete(id);
    }

    public int totalCount() {
        return userMapper.totalCount();
    }
}
