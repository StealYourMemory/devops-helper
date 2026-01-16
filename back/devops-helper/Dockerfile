# 基础镜像
FROM openjdk:21-jdk

# 设置工作目录
WORKDIR /app

# 将应用程序复制到工作目录
COPY target/devops-helper-1.0.jar /app/app.jar

# 设置时区
ENV TZ=Asia/Shanghai


# 暴露容器端口
EXPOSE 8765 8766

# 配置容器启动命令
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8766", "-jar", "app.jar"]
