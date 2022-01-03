package com.bolingcavalry.grabpush.bean.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2022/1/1 16:01
 */
@Data
public class Result {

    @JsonProperty("face_num")
    private int faceNum;
    @JsonProperty("face_list")
    List<Face> faceList;
}
