# 8-jdk-alpine版本启动的时候会crash
FROM openjdk:8u292-jdk

# 创建目录
RUN mkdir -p /app/images && mkdir -p /app/model

# 指定镜像的内容的来源位置
ARG DEPENDENCY=target/dependency

# 复制内容到镜像
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

# 指定启动命令
ENTRYPOINT ["java","-cp","app:app/lib/*","com.bolingcavalry.predictnumber.PredictNumberApplication"]