FROM openjdk:8-alpine
EXPOSE 8101
COPY target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]