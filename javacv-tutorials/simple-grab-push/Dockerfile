# 基础镜像集成了openjdk8和opencv4.5.3
FROM bolingcavalry/opencv4.5.3:0.0.1

# 指定镜像的内容的来源位置
ARG DEPENDENCY=target/dependency

# 复制内容到镜像
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENV LANG C.UTF-8
ENV LANGUAGE zh_CN.UTF-8
ENV LC_ALL C.UTF-8
ENV TZ Asia/Shanghai

# 指定启动命令(注意要执行编码，否则日志是乱码)
ENTRYPOINT ["java","-Dfile.encoding=utf-8","-cp","app:app/lib/*","com.bolingcavalry.grabpush.SimpleGrabPushApplication"]