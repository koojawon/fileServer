FROM amazoncorretto:17.0.7-alpine
ARG JAR_FILE=/build/libs/FlatServer-0.0.1-BETA.jar
COPY ${JAR_FILE} /flatserver.jar
ENTRYPOINT ["java","-jar","/flatserver.jar"]