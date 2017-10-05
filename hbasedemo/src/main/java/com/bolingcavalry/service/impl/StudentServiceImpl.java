package com.bolingcavalry.service.impl;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.bean.Student;
import com.bolingcavalry.dto.StudentDTO;
import com.bolingcavalry.service.StudentService;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 学生服务类的实现
 * @email zq2599@gmail.com
 * @Date 2017/10/5 上午9:47
 */
@Service
public class StudentServiceImpl implements StudentService{

    protected static final Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);

    private static final String TABLE_NAME = "student";

    private static final String[] COLUMNS = {"id", "info"};

    private static final String ERROR_CREATE_HTABLE_FAIL = "create htable fail";

    private Configuration configuration;

    private HBaseAdmin hBaseAdmin;

    @PostConstruct
    public void init(){
        LOGGER.info("start init");
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hbaseserver");
        try {
            hBaseAdmin = new HBaseAdmin(configuration);

            //检查表是否存在，如果不存在就创建
            if (!hBaseAdmin.tableExists(TABLE_NAME)) {
                LOGGER.info("table is not exist");

                HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
                for (String columnName : COLUMNS) {
                    HColumnDescriptor column = new HColumnDescriptor(columnName);
                    hTableDescriptor.addFamily(column);
                }

                hBaseAdmin.createTable(hTableDescriptor);
            }
        }catch (Exception e){
            LOGGER.error("init error", e);
            e.printStackTrace();
        }

        LOGGER.info("finish init");
    }

    private HTable htable(){
        HTable hTable = null;
        try {
            hTable = new HTable(configuration, TableName.valueOf(TABLE_NAME));
        }catch(Exception exception){
            LOGGER.error("create HTable error, ", exception);
            exception.printStackTrace();
        }

        return hTable;
    }


    public String insert(Student student) {
        LOGGER.info("start insert : {}", JSON.toJSONString(student));

        String rowId = String.valueOf(student.getId());
        String id = rowId;

        //主键
        byte[] rowIdBytes = Bytes.toBytes(rowId);

        String errorStr = null;
        HTable hTable = htable();

        if(null==hTable){
            return ERROR_CREATE_HTABLE_FAIL;
        }

        Put p1 = new Put(rowIdBytes);

        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id);
        map.put("info:name", student.getName());
        map.put("info:age", String.valueOf(student.getAge()));

        for (String columnName : map.keySet()) {
            byte[] value = Bytes.toBytes(map.get(columnName));
            String[] str = columnName.split(":");
            byte[] family = Bytes.toBytes(str[0]);
            byte[] qualifier = null;
            if (str.length > 1) {
                qualifier = Bytes.toBytes(str[1]);
            }
            p1.addColumn(family, qualifier, value);
        }

        try {
            hTable.put(p1);
        }catch(Exception e){
            LOGGER.error("insert error : " + e);
            e.printStackTrace();
        }


        LOGGER.info("finish insert");

        return errorStr;
    }

    public String delete(long id) {
        LOGGER.info("start delete [{}]", id);
        HTable hTable = htable();

        if(null==hTable){
            return ERROR_CREATE_HTABLE_FAIL;
        }

        List<Delete> list = new ArrayList<Delete>();
        Delete delete = new Delete(Bytes.toBytes(String.valueOf(id)));
        list.add(delete);

        String errorStr = null;

        try {
            hTable.delete(list);
        }catch(Exception e){
            errorStr = "delete error : " + e;
            LOGGER.error("delete error");
        }

        return errorStr;
    }

    public StudentDTO find(long id) {
        LOGGER.info("start find [{}]", id);
        HTable hTable = htable();

        if(null==hTable){
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setErrorStr(ERROR_CREATE_HTABLE_FAIL);
            return studentDTO;
        }

        String errorStr = null;
        Result result = null;
        Get get = new Get(Bytes.toBytes(String.valueOf(id)));

        try {
            result = hTable.get(get);
        }catch(Exception e){
            errorStr = "find error : " + e;
        }

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setErrorStr(errorStr);

        if(StringUtils.isBlank(errorStr) && null!=result){
            studentDTO.setStudent(buildStudent(result.rawCells()));
            LOGGER.info("after build : " + JSON.toJSONString(studentDTO));
        }

        return studentDTO;
    }

    /**
     * 根据结果构造一个student对象
     * @param cellArray
     * @return
     */
    private static Student buildStudent(Cell[] cellArray){
        if(null==cellArray || cellArray.length<1){
            LOGGER.error("invalid cell array for build student");
            return null;
        }

        LOGGER.error("start buildStudent");

        Student student = null;

        for(Cell cell : cellArray){
            String row = new String(CellUtil.cloneRow(cell));
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            String value = new String(CellUtil.cloneValue(cell));

            LOGGER.info("row [{}], family [{}], qualifier [{}], value [{}]", row, family, qualifier, value);

            if(!StringUtils.isNumeric(row)){
                LOGGER.error("invalid row for build student");
                return null;
            }

            if(null==student){
                student = new Student();
                student.setId(Long.valueOf(row));
            }

            if("info".equals(family)){
                if("age".equals(qualifier)){
                    student.setAge(Integer.valueOf(value));
                } else if("name".equals(qualifier)){
                    student.setName(value);
                }
            }
        }

        LOGGER.info("build : " + JSON.toJSONString(student));

        return student;
    }
}
