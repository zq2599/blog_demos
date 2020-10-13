package com.bolingcavalry.relatedoperation.mapper;

import com.bolingcavalry.relatedoperation.entity.LogAssociateUser;
import com.bolingcavalry.relatedoperation.entity.LogExtend;
import org.springframework.stereotype.Repository;

/**
 * @Description: log表的mapper
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:32
 */

@Repository
public interface LogMapper {

    LogExtend oneObjectSel(int id);

    LogAssociateUser leftJoinSel(int id);

    LogAssociateUser nestedSel(int id);
}
