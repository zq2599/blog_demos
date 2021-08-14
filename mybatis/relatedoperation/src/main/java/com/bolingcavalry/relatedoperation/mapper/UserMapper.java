package com.bolingcavalry.relatedoperation.mapper;

import com.bolingcavalry.relatedoperation.entity.UserWithLogs;
import org.springframework.stereotype.Repository;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:32
 */

@Repository
public interface UserMapper {

    UserWithLogs leftJoinSel(int id);

    UserWithLogs nestedSel(int id);
}
