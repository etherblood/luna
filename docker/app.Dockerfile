FROM eclipse-temurin:23-jre-alpine
WORKDIR /home
COPY target/application-server-0.1.0.jar ./
COPY target/libs libs
ENTRYPOINT ["java", "-jar", "application-server-0.1.0.jar"]