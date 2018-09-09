package com.bolingcavalry.customizeimportselector.selector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Description: 自定义selector3
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/7 6:40
 */
public class CustomizeImportSelector3 implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        System.out.println("selectImports : " + this.getClass().getSimpleName());
        return new String[]{"com.bolingcavalry.customizeimportselector.service.impl.CustomizeServiceImpl3"};
    }
}
