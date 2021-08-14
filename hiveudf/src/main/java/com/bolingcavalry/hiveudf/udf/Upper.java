package com.bolingcavalry.hiveudf.udf;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDF;

public class Upper extends UDF {

    /**
     * 如果入参是合法字符串，就转为小写返回
     * @param str
     * @return
     */
    public String evaluate(String str) {
        return StringUtils.isBlank(str) ? str : str.toUpperCase();
    }
}
