package com.bolingcavalry.curd.service;

import com.bolingcavalry.curd.entity.Log;
import com.bolingcavalry.curd.entity.LogExtend;
import com.bolingcavalry.curd.entity.User;
import com.bolingcavalry.curd.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@Service
public class LogService {
    @Autowired
    LogMapper logMapper;

    public Log sel(int id){
        return logMapper.sel(id);
    }

    public LogExtend selExtend(int id) {
        return logMapper.selExtend(id);
    }

    public Log insertWithFields(Log log) {
        logMapper.insertWithFields(log);
        return log;
    }

}
