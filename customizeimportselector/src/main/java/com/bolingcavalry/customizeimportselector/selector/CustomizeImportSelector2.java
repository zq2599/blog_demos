package com.bolingcavalry.customizeimportselector.selector;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Description: 自定义selector2
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/7 6:40
 */
@Order(101)
public class CustomizeImportSelector2 implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        System.out.println("selectImports : " + this.getClass().getSimpleName());
        return new String[]{"com.bolingcavalry.customizeimportselector.service.impl.CustomizeServiceImpl2"};
    }
}
