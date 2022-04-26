FROM openjdk:8-alpine

EXPOSE 8101

COPY target/app.jar app.jar

CMD ["java", "-jar", "app.jar"]
