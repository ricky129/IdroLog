FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/idrolog-server-*.jar app.jar
EXPOSE 7000
CMD ["java", "-jar", "app.jar"]