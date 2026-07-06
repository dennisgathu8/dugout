FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/dugout-0.1.0-SNAPSHOT-standalone.jar app.jar
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
