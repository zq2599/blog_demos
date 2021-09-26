package com.bolingcavalry.annonation.service.impl;

import com.bolingcavalry.annonation.aop.MySpan;
import com.bolingcavalry.annonation.service.Biz;
import com.bolingcavalry.annonation.service.ChildBiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/25 10:59 上午
 * @description 功能介绍
 */
@Component
@Slf4j
public class BizImpl implements Biz {

    final ChildBiz childBiz;

    public BizImpl(ChildBiz childBiz) {
        this.childBiz = childBiz;
    }

    @Override
    @MySpan(spanName = "aaabbb")
    public void mock() {
      log.info("mock");
      childBiz.mockChild();
    }
}
