FROM openjdk:11-jdk

EXPOSE 8080
COPY target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -jar /app.jar"]