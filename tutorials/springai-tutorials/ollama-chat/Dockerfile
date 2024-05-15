# 使用Spring Boot官方镜像作为基础镜像
FROM openjdk:17-jdk-slim

# 设置环境变量
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS=""

# 复制项目jar文件到Docker镜像中
COPY target/*.jar /app.jar

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.config.location=file:/app/application.properties"]