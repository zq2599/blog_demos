package com.bolingcavalry.grabpush.bean.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2022/1/1 13:30
 */
@Data
@ToString
public class FaceDetectResponse implements Serializable {
    @JsonProperty("error_code")
    String errorCode;
    @JsonProperty("error_msg")
    String errorMsg;
    Result result;
}
