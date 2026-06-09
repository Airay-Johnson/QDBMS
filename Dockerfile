# QDBMS 生产镜像 — Spring Boot 内嵌前端静态文件
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app

# 复制已构建的 JAR（由 GitHub Actions 预构建）
COPY qdbms-server/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=mysql

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/ || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
