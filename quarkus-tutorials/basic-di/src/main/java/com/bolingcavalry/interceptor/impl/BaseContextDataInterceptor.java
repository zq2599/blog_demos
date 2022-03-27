package com.bolingcavalry.interceptor.impl;

import io.quarkus.logging.Log;

import javax.interceptor.InvocationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bolingcavalry.interceptor.define.ContextData.KEY_PROCEED_INTERCEPTORS;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description ContextData的实现
 */
public class BaseContextDataInterceptor {

    Object execute(InvocationContext context) throws Exception {
        // 取出保存拦截器间共享数据的map
        Map<String, Object> map = context.getContextData();

        List<String> list;

        String instanceClassName = this.getClass().getSimpleName();

        // 根据指定key从map中获取一个list
        if (map.containsKey(KEY_PROCEED_INTERCEPTORS)) {
            list = (List<String>) map.get(KEY_PROCEED_INTERCEPTORS);
        } else {
            // 如果map中没有，就在此新建一个list，存如map中
            list = new ArrayList<>();
            map.put(KEY_PROCEED_INTERCEPTORS, list);

            Log.infov("from {0}, this is first processor", instanceClassName);
        }

        // 将自身内容存入list中，这样下一个拦截器只要是BaseContextDataInterceptor的子类，
        // 就能取得前面所有执行过拦截操作的拦截器
        list.add(instanceClassName);

        Log.infov("From {0}, all processors {0}", instanceClassName, list);

        return context.proceed();
    }
}
