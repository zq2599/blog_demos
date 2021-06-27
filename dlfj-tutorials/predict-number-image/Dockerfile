FROM openjdk:8-jdk-alpine
# 增加群组和用户
RUN addgroup -S spring && adduser -S spring -G spring
# 指定容器运行时1号进程的用户和群组
USER spring:spring
# 指定镜像的内容的来源位置
ARG DEPENDENCY=build/dependency
# 复制内容到镜像
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
# 指定启动命令
ENTRYPOINT ["java","-cp","app:app/lib/*","com.bolingcavalry.predictnumber.PredictNumberApplication"]