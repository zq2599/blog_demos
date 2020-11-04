package com.bolingcavalry.hiveudf.udaf;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.io.LongWritable;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/11/4 9:57
 */
public class FieldLengthUDAFEvaluator extends GenericUDAFEvaluator {

    PrimitiveObjectInspector inputOI;
    ObjectInspector outputOI;
    PrimitiveObjectInspector integerOI;

    int total = 0;


    @Override
    public ObjectInspector init(Mode m, ObjectInspector[] parameters) throws HiveException {
        super.init(m, parameters);

        if(Mode.PARTIAL1.equals(m) || Mode.COMPLETE.equals(m)) {
            inputOI = (PrimitiveObjectInspector) parameters[0];
        } else {
            integerOI = (PrimitiveObjectInspector) parameters[0];
        }

        outputOI = ObjectInspectorFactory.getReflectionObjectInspector(
                Integer.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA
        );

        return outputOI;
    }

    public AggregationBuffer getNewAggregationBuffer() throws HiveException {
        return new FieldLengthAggregationBuffer();
    }


    public void reset(AggregationBuffer agg) throws HiveException {
        ((FieldLengthAggregationBuffer)agg).setValue(0);
    }



    public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
        if(null==parameters || parameters.length<1) {
            return;
        }

        Object javaObj = inputOI.getPrimitiveJavaObject(parameters[0]);

        ((FieldLengthAggregationBuffer)agg).add(String.valueOf(javaObj).length());
    }

    public Object terminate(AggregationBuffer agg) throws HiveException {
        return ((FieldLengthAggregationBuffer)agg).getValue();
    }


    public Object terminatePartial(AggregationBuffer agg) throws HiveException {
        return terminate(agg);
    }

    public void merge(AggregationBuffer agg, Object partial) throws HiveException {

        ((FieldLengthAggregationBuffer) agg).add((Integer)integerOI.getPrimitiveJavaObject(partial));
    }

}
