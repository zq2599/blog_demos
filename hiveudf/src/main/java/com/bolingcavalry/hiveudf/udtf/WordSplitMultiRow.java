package com.bolingcavalry.hiveudf.udtf;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 把指定字段拆成多行，每行有多列
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/11/5 14:43
 */
public class WordSplitMultiRow extends GenericUDTF {

    private PrimitiveObjectInspector stringOI = null;


    private final static String[] EMPTY_ARRAY = {"NULL", "NULL", "NULL"};

    /**
     * 一列拆成多列的逻辑在此
     * @param args
     * @throws HiveException
     */
    @Override
    public void process(Object[] args) throws HiveException {
        String input = stringOI.getPrimitiveJavaObject(args[0]).toString();

        // 无效字符串
        if(StringUtils.isBlank(input)) {
            forward(EMPTY_ARRAY);
        } else {

            // 用逗号分隔
            String[] rowArray = input.split(",");

            // 处理异常
            if(null==rowArray || rowArray.length<1) {
                String[] errRlt = new String[3];
                errRlt[0] = input;
                errRlt[1] = "can not split to valid row array";
                errRlt[2] = "-";

                forward(errRlt);
            } else {
                // rowArray的每个元素，都是"id:key:value"这样的字符串
                for(String singleRow : rowArray) {

                    // 要确保字符串有效
                    if(StringUtils.isBlank(singleRow)) {
                        forward(EMPTY_ARRAY);
                    } else {
                        // 分割字符串
                        String[] array = singleRow.split(":");

                        // 如果字符串数组不合法，就返回原始字符串和错误提示
                        if(null==array || array.length<3) {
                            String[] errRlt = new String[3];
                            errRlt[0] = input;
                            errRlt[1] = "can not split to valid array";
                            errRlt[2] = "-";

                            forward(errRlt);
                        } else {
                            forward(array);
                        }
                    }
                }

            }
        }
    }

    /**
     * 释放资源在此执行，本例没有资源需要释放
     * @throws HiveException
     */
    @Override
    public void close() throws HiveException {

    }

    @Override
    public StructObjectInspector initialize(StructObjectInspector argOIs) throws UDFArgumentException {

        List<? extends StructField> inputFields = argOIs.getAllStructFieldRefs();

        // 当前UDTF只处理一个参数，在此判断传入的是不是一个参数
        if (1 != inputFields.size()) {
            throw new UDFArgumentLengthException("ExplodeMap takes only one argument");
        }

        // 此UDTF只处理字符串类型
        if(!Category.PRIMITIVE.equals(inputFields.get(0).getFieldObjectInspector().getCategory())) {
            throw new UDFArgumentException("ExplodeMap takes string as a parameter");
        }

        stringOI = (PrimitiveObjectInspector)inputFields.get(0).getFieldObjectInspector();

        //列名集合
        ArrayList<String> fieldNames = new ArrayList<String>();

        //列对应的value值
        ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();

        // 第一列的列名
        fieldNames.add("id");
        // 第一列的inspector类型为string型
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        // 第二列的列名
        fieldNames.add("key");
        // 第二列的inspector类型为string型
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        // 第三列的列名
        fieldNames.add("value");
        // 第三列的inspector类型为string型
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }
}
