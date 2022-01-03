package com.bolingcavalry.grabpush.bean.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2022/1/1 16:21
 */
@Data
public class FaceDetectRequest {
    @JsonProperty("image_type")
    String imageType;
    @JsonProperty("face_field")
    String faceField;
    @JsonProperty("max_face_num")
    int maxFaceNum;
    @JsonProperty("face_type")
    String faceType;
    @JsonProperty("liveness_control")
    String livenessControl;
    @JsonProperty("face_sort_type")
    int faceSortType;
    String image;
}
