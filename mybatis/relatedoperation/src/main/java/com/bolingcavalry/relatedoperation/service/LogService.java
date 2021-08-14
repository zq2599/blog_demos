package com.bolingcavalry.relatedoperation.service;

import com.bolingcavalry.relatedoperation.entity.LogAssociateUser;
import com.bolingcavalry.relatedoperation.entity.LogExtend;
import com.bolingcavalry.relatedoperation.mapper.LogMapper;
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

    public LogExtend oneObjectSel(int id){
        return logMapper.oneObjectSel(id);
    }

    public LogAssociateUser leftJoinSel(int id){
        return logMapper.leftJoinSel(id);
    }

    public LogAssociateUser nestedSel(int id){
        return logMapper.nestedSel(id);
    }
}
