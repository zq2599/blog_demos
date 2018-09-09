package com.bolingcavalry.customizeimportselector;

import com.bolingcavalry.customizeimportselector.selector.CustomizeImportSelector1;
import com.bolingcavalry.customizeimportselector.selector.CustomizeImportSelector2;
import com.bolingcavalry.customizeimportselector.selector.CustomizeImportSelector3;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Description: 系统配置类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/9 13:42
 */
@Configuration
@Import({CustomizeImportSelector1.class, CustomizeImportSelector2.class, CustomizeImportSelector3.class})
public class SysConfig {
}
