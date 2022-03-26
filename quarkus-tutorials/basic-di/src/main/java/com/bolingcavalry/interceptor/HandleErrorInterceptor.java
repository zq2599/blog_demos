package com.bolingcavalry.interceptor;

import com.bolingcavalry.interceptor.meta.HandleError;
import io.quarkus.arc.Priority;
import javax.interceptor.Interceptor;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 22:36
 * @description HandleError的实现
 */
@HandleError
@Interceptor
@Priority(Interceptor.Priority.APPLICATION +1)
public class HandleErrorInterceptor {

}
