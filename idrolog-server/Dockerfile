FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/app-shaded.jar app.jar
EXPOSE 7000
CMD ["java", "-jar", "app.jar"]