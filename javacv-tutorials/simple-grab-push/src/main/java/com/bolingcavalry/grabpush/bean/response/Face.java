package com.bolingcavalry.grabpush.bean.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2022/1/1 16:03
 */
@Data
public class Face {
    Location location;
    @JsonProperty("face_probability")
    double face_probability;
    Mask mask;
}
