package com.bolingcavalry.relatedoperation.service;

import com.bolingcavalry.relatedoperation.entity.UserWithLogs;
import com.bolingcavalry.relatedoperation.mapper.UserMapper;
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

    public UserWithLogs leftJoinSel(int id) {
        return userMapper.leftJoinSel(id);
    }

    public UserWithLogs nestedSel(int id) {
        return userMapper.nestedSel(id);
    }
}
