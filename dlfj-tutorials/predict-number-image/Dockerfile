FROM openjdk:8-jdk-alpine

# 增加群组和用户
RUN addgroup -S spring && adduser -S spring -G spring
# 创建目录
RUN mkdir -p /app/images && mkdir -p /app/model && chown -R spring:spring /app/images/ && chown -R spring:spring /app/model/
# 指定容器运行时1号进程的用户和群组
USER spring:spring
# 指定镜像的内容的来源位置
ARG DEPENDENCY=target/dependency
# 复制内容到镜像
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
# 指定启动命令
ENTRYPOINT ["java","-cp","app:app/lib/*","com.bolingcavalry.predictnumber.PredictNumberApplication"]