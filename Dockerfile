FROM tomcat:9.0.62-jdk8-openjdk
LABEL maintener=EXEMPLE
COPY exemple-service-api-launcher/target/*.war /usr/local/tomcat/webapps/ExempleService.war
COPY exemple-service-api-launcher/src/main/conf/context.xml /usr/local/tomcat/conf/context.xml
COPY exemple-service-api-launcher/src/main/conf/setenv.sh /usr/local/tomcat/bin/setenv.sh
CMD ["catalina.sh", "run"]