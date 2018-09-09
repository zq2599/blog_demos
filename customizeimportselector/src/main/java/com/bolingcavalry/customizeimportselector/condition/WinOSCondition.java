package com.bolingcavalry.customizeimportselector.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @Description: 当前系统如果是windows，就返回true
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/9 9:46
 */
public class WinOSCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return conditionContext.getEnvironment().getProperty("os.name").contains("Windows");
    }
}
