package com.bolingcavalry.grabpush.bean.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2022/1/1 13:30
 */
@Data
@ToString
public class FaceDetectResponse implements Serializable {
    // 返回码
    @JsonProperty("error_code")
    String errorCode;
    // 描述信息
    @JsonProperty("error_msg")
    String errorMsg;
    // 返回的具体内容
    Result result;

    /**
     * @author willzhao
     * @version 1.0
     * @description 返回的具体内容
     * @date 2022/1/1 16:01
     */
    @Data
    public static class Result {
        // 人脸数量
        @JsonProperty("face_num")
        private int faceNum;
        // 每个人脸的信息
        @JsonProperty("face_list")
        List<Face> faceList;

        /**
         * @author willzhao
         * @version 1.0
         * @description 检测出来的人脸对象
         * @date 2022/1/1 16:03
         */
        @Data
        public static class Face {
            // 位置
            Location location;
            // 是人脸的置信度
            @JsonProperty("face_probability")
            double face_probability;
            // 口罩
            Mask mask;

            /**
             * @author willzhao
             * @version 1.0
             * @description 人脸在图片中的位置
             * @date 2022/1/1 16:04
             */
            @Data
            public static class Location {
                double left;
                double top;
                double width;
                double height;
                double rotation;
            }

            /**
             * @author willzhao
             * @version 1.0
             * @description 口罩对象
             * @date 2022/1/1 16:11
             */
            @Data
            public static class Mask {
                int type;
                double probability;
            }
        }
    }
}
