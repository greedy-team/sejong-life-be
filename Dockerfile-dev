FROM eclipse-temurin:17.0.12_7-jre
WORKDIR /app
COPY build/libs/server.jar /app/server.jar
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/server.jar"]
