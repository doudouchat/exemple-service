ARG VERSION_TOMCAT
FROM tomcat:$VERSION_TOMCAT
LABEL maintener=EXEMPLE
COPY exemple-service-api-launcher/target/*.war /usr/local/tomcat/webapps/ExempleService.war
COPY exemple-service-api-launcher/src/main/conf/context.xml /usr/local/tomcat/conf/context.xml
COPY exemple-service-api-launcher/src/main/conf/setenv.sh /usr/local/tomcat/bin/setenv.sh
CMD ["catalina.sh", "run"]