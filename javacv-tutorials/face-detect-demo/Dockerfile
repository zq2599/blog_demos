# 基础镜像集成了openjdk8和opencv3.4.3
FROM bolingcavalry/opencv3.4.3:0.0.3

# 创建目录
RUN mkdir -p /app/images && mkdir -p /app/model

# 指定镜像的内容的来源位置
ARG DEPENDENCY=target/dependency

# 复制内容到镜像
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

# 指定启动命令
ENTRYPOINT ["java","-Djava.library.path=/opencv-3.4.3/build/lib","-cp","app:app/lib/*","com.bolingcavalry.facedetect.FaceDetectApplication"]