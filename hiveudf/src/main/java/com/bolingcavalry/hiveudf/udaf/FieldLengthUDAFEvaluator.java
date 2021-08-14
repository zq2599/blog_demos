package com.bolingcavalry.hiveudf.udaf;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;

/**
 * @Description: 这里是UDAF的实际处理类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/11/4 9:57
 */
public class FieldLengthUDAFEvaluator extends GenericUDAFEvaluator {

    PrimitiveObjectInspector inputOI;

    ObjectInspector outputOI;

    PrimitiveObjectInspector integerOI;

    /**
     * 每个阶段都会被执行的方法，
     * 这里面主要是把每个阶段要用到的输入输出inspector好，其他方法被调用时就能直接使用了
     * @param m
     * @param parameters
     * @return
     * @throws HiveException
     */
    @Override
    public ObjectInspector init(Mode m, ObjectInspector[] parameters) throws HiveException {
        super.init(m, parameters);

        // COMPLETE或者PARTIAL1，输入的都是数据库的原始数据
        if(Mode.PARTIAL1.equals(m) || Mode.COMPLETE.equals(m)) {
            inputOI = (PrimitiveObjectInspector) parameters[0];
        } else {
            // PARTIAL2和FINAL阶段，都是基于前一个阶段init返回值作为parameters入参
            integerOI = (PrimitiveObjectInspector) parameters[0];
        }

        outputOI = ObjectInspectorFactory.getReflectionObjectInspector(
                Integer.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA
        );

        // 给下一个阶段用的，即告诉下一个阶段，自己输出数据的类型
        return outputOI;
    }

    public AggregationBuffer getNewAggregationBuffer() throws HiveException {
        return new FieldLengthAggregationBuffer();
    }


    /**
     * 重置，将总数清理掉
     * @param agg
     * @throws HiveException
     */
    public void reset(AggregationBuffer agg) throws HiveException {
        ((FieldLengthAggregationBuffer)agg).setValue(0);
    }


    /**
     * 不断被调用执行的方法，最终数据都保存在agg中
     * @param agg
     * @param parameters
     * @throws HiveException
     */
    public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
        if(null==parameters || parameters.length<1) {
            return;
        }

        Object javaObj = inputOI.getPrimitiveJavaObject(parameters[0]);

        ((FieldLengthAggregationBuffer)agg).add(String.valueOf(javaObj).length());
    }

    /**
     * group by的时候返回当前分组的最终结果
     * @param agg
     * @return
     * @throws HiveException
     */
    public Object terminate(AggregationBuffer agg) throws HiveException {
        return ((FieldLengthAggregationBuffer)agg).getValue();
    }

    /**
     * 当前阶段结束时执行的方法，返回的是部分聚合的结果（map、combiner）
     * @param agg
     * @return
     * @throws HiveException
     */
    public Object terminatePartial(AggregationBuffer agg) throws HiveException {
        return terminate(agg);
    }

    /**
     * 合并数据，将总长度加入到缓存对象中（combiner或reduce）
     * @param agg
     * @param partial
     * @throws HiveException
     */
    public void merge(AggregationBuffer agg, Object partial) throws HiveException {

        ((FieldLengthAggregationBuffer) agg).add((Integer)integerOI.getPrimitiveJavaObject(partial));
    }

}
