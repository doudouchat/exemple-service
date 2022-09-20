ARG VERSION_OPENJDK
FROM openjdk:$VERSION_OPENJDK
LABEL maintener=EXEMPLE_TEST_AUTHORIZATION_SERVER
COPY target/*.jar exemple-service-integration-authorization-server.jar
ENTRYPOINT ["java","-jar","exemple-service-integration-authorization-server.jar"]