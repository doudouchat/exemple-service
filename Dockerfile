FROM tomcat:8.5.32-jre8
LABEL maintener=EXEMPLE
COPY exemple-service-api/target/*.war /usr/local/tomcat/webapps/ExempleService.war
COPY exemple-service-api/src/main/conf/context.xml /usr/local/tomcat/conf/context.xml
COPY exemple-service-api/src/main/conf/setenv.sh /usr/local/tomcat/bin/setenv.sh
CMD ["catalina.sh", "run"]